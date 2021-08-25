@file:Suppress("UNCHECKED_CAST")

package com.skoumal.grimoire.transfusion.live

import android.app.Activity
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

typealias BindingLayoutCreator<B> = (View) -> B

fun interface OnDestroyBindingListener<B : ViewBinding> {
    fun onDestroyBinding(binding: B)
}

class InLifecycleOwnerBinder<B : ViewBinding>(
    private val creator: BindingLayoutCreator<B>,
    private val beforeDestroy: OnDestroyBindingListener<B>? = null
) : ReadOnlyProperty<LifecycleOwner, B>, LifecycleObserver {

    private var binding: B? = null

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): B {
        val viewOwner = when (thisRef) {
            is Fragment -> thisRef.viewLifecycleOwner
            else -> thisRef
        }

        require(viewOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))

        return this.binding ?: synchronized(this) {
            this.binding ?: tryCreateBinding(thisRef)
                .applyLifecycleOwner(viewOwner)
                .also { this.binding = it }
        }
    }

    private fun tryCreateBinding(owner: LifecycleOwner): B {
        return when (owner) {
            is Fragment -> createBinding(owner)
            is Activity -> createBinding(owner)
            else -> throw IllegalArgumentException("Unknown lifecycle owner")
        }
    }

    // ---

    private fun B.applyLifecycleOwner(owner: LifecycleOwner) = apply {
        owner.lifecycle.addObserver(this@InLifecycleOwnerBinder)
        if (this is ViewDataBinding) {
            lifecycleOwner = owner
        }
    }

    // ---

    private fun createBinding(fragment: Fragment): B {
        return creator(fragment.requireView())
    }

    private fun createBinding(activity: Activity): B {
        val view = activity.window.decorView
            .findViewById<ViewGroup>(android.R.id.content)
            .getChildAt(0)
        return creator(view)
    }

    // ---

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)

        when (val b = binding) {
            null -> return
            is ViewDataBinding -> {
                beforeDestroy?.onDestroyBinding(b)
                b.unbind()
            }
            else -> {
                beforeDestroy?.onDestroyBinding(b)
            }
        }

        binding = null
    }

}

/**
 * Creates safe [ViewBinding] based binding object which will automatically dispose once lifecycle
 * reaches [Lifecycle.State.DESTROYED] and is automatically created on the first invocation of
 * [getValue]. [getValue] cannot be called before [Lifecycle.State.INITIALIZED] due to obvious
 * view-based limitations.
 * */
fun <B : ViewBinding> viewBinding(
    beforeDestroy: OnDestroyBindingListener<B>? = null,
    creator: BindingLayoutCreator<B>
): ReadOnlyProperty<LifecycleOwner, B> =
    InLifecycleOwnerBinder(creator, beforeDestroy)
