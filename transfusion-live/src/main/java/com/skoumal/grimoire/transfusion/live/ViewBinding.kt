@file:Suppress("UNCHECKED_CAST")

package com.skoumal.grimoire.transfusion.live

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class InViewBinder<B : ViewBinding>(
    klass: Class<B>,
    parent: ViewGroup
) : ReadOnlyProperty<ViewGroup, B> {

    private val binding: B = try {
        val method = klass.getMethod(
            INFLATE,
            LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        method.invoke(null, LayoutInflater.from(parent.context), parent, true) as B
    } catch (e: NoSuchElementException) {
        val method = klass.getMethod(
            INFLATE,
            LayoutInflater::class.java, ViewGroup::class.java
        )
        method.invoke(null, LayoutInflater.from(parent.context), parent) as B
    }

    override fun getValue(thisRef: ViewGroup, property: KProperty<*>): B {
        return binding
    }

    companion object {

        private const val INFLATE = "inflate"

    }

}

class InLifecycleOwnerBinder<B : ViewBinding>(
    klass: Class<B>,
    owner: LifecycleOwner
) : ReadOnlyProperty<LifecycleOwner, B>, LifecycleObserver {

    private var binding: B? = null
    private val method = klass.getMethod(BIND, View::class.java)

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): B {
        require(thisRef.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))

        val binding = binding ?: synchronized(this) {
            binding ?: tryCreateBinding(thisRef).also { binding = it }
        }

        if (binding is ViewDataBinding) {
            binding.lifecycleOwner = findLifecycleOwner(thisRef)
        }

        return binding
    }

    private fun tryCreateBinding(owner: LifecycleOwner): B {
        return when (owner) {
            is Fragment -> createBinding(owner)
            is Activity -> createBinding(owner)
            else -> throw IllegalArgumentException("Unknown lifecycle owner")
        }
    }

    private fun findLifecycleOwner(owner: LifecycleOwner): LifecycleOwner {
        return when (owner) {
            is Fragment -> owner.viewLifecycleOwner
            else -> owner
        }
    }

    private fun createBinding(fragment: Fragment): B {
        return method.invoke(null, fragment.requireView()) as B
    }

    private fun createBinding(activity: Activity): B {
        val view = activity.window.decorView
            .findViewById<ViewGroup>(android.R.id.content)
            .getChildAt(0)
        return method.invoke(null, view) as B
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        val b = binding ?: return
        if (b is ViewDataBinding) {
            b.unbind()
        }
        binding = null
    }

    companion object {

        private const val BIND = "bind"

    }

}

/**
 * Creates safe [ViewBinding] based binding object which won't be automatically disposed, but
 * automatically created upon invoking this method.
 * */
inline fun <reified B : ViewBinding> ViewGroup.viewBinding(): ReadOnlyProperty<ViewGroup, B> =
    InViewBinder(B::class.java, this)

/**
 * Creates safe [ViewBinding] based binding object which will automatically dispose once lifecycle
 * reaches [Lifecycle.State.DESTROYED] and is automatically created on the first invocation of
 * [getValue]. [getValue] cannot be called before [Lifecycle.State.INITIALIZED] due to obvious
 * view-based limitations.
 * */
inline fun <reified B : ViewBinding> LifecycleOwner.viewBinding(): ReadOnlyProperty<LifecycleOwner, B> =
    InLifecycleOwnerBinder(B::class.java, this)