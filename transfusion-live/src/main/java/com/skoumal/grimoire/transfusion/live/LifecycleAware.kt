package com.skoumal.grimoire.transfusion.live

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Property allows consumers to create value dynamically with [creator] and dispose it automatically
 * with lifecycle hooks. Though this is designed to dispose the fields, it will only release the
 * stored [value].
 *
 * Release of the stored value is performed on [onDestroy] lifecycle callback and the value is then
 * initialized only if the lifecycle returns to _at least_ [Lifecycle.State.CREATED] state.
 *
 * This is only useful for values that **hold local context or views**! Using it for whatever else
 * types and values is an overkill and **wastes runtime memory**. Meaning - use this for adapters.
 *
 * @throws IllegalArgumentException If accessed beyond the lifecycle scope (ie. [Lifecycle.State.DESTROYED])
 * @throws Throwable If [creator] fails to provide the object it rethrows the exception
 * */
class LifecycleAware<T : Any> internal constructor(
    private val creator: () -> T,
    owner: LifecycleOwner
) : ReadOnlyProperty<LifecycleOwner, T>, LifecycleObserver {

    private var value: T? = null

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        require(thisRef.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            "Fetching this value requires at least initialized state!"
        }

        return synchronized(this) {
            value ?: creator().also { value = it }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        value = null
    }

}

/**
 * Property allows consumers to assign and fetch values and dispose it automatically with lifecycle
 * hooks. Though this is designed to dispose the fields, it will only release the stored [value].
 *
 * Release of the stored value is performed on [onDestroy] lifecycle callback and the value is then
 * initialized only if the lifecycle returns to _at least_ [Lifecycle.State.CREATED] state.
 *
 * This is only useful for values that **hold local context or views**! Using it for whatever else
 * types and values is an overkill and **wastes runtime memory**. Meaning - use this for adapters.
 *
 * Value can be assigned multiple times, keeping no history whatsoever. If, however, consumer
 * assigns the value in [Lifecycle.State.DESTROYED] state, it will be ignored, as callback has
 * already been called and it would likely cause memory leaks.
 *
 * If you need to check whether the value was initialized call [getLifecycleAwareDelegate] on your
 * property and check with [isNotNull].
 *
 * ```kotlin
 * var prop by lifecycleAware()
 *
 * fun assignIfNotInitialized() {
 *     val isNotNull = this::prop.getLifecycleAwareDelegate()?.isNotNull
 *     // handle the assignment
 * }
 * ```
 *
 * @throws IllegalArgumentException If accessed beyond the lifecycle scope (ie. [Lifecycle.State.DESTROYED])
 * */
class MutableLifecycleAware<T : Any> internal constructor(
    default: T? = null,
    owner: LifecycleOwner
) : ReadWriteProperty<LifecycleOwner, T>, LifecycleObserver {

    @Volatile
    private var value: T? = default

    val isNotNull get() = value != null

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        val lifecycle = thisRef.lifecycle
        require(lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            "Cannot return value before lifecycle initializes"
        }
        return value
            ?: throw IllegalStateException("Cannot return internal value. It's never been assigned.")
    }

    override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: T) {
        val lifecycle = thisRef.lifecycle
        if (lifecycle.currentState <= Lifecycle.State.DESTROYED) {
            Log.e(
                "MutableLifecycleAware",
                "Saving value to ${thisRef::class.java} is not permitted after being destroyed. Ignoring value silently…"
            )
            return
        }

        this.value = value
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        value = null
    }

}

/**
 * Provides common way to initialize [LifecycleAware] or [MutableLifecycleAware].
 * @see LifecycleAware
 * @see MutableLifecycleAware
 * */
fun <T : Any> LifecycleOwner.lifecycleAware(creator: () -> T): ReadOnlyProperty<LifecycleOwner, T> =
    LifecycleAware(creator, this)

/**
 * Provides common way to initialize [LifecycleAware] or [MutableLifecycleAware].
 * @see LifecycleAware
 * @see MutableLifecycleAware
 * */
fun <T : Any> LifecycleOwner.lifecycleAware(default: T? = null): ReadWriteProperty<LifecycleOwner, T> =
    MutableLifecycleAware(default, this)

/**
 * Provides common way to initialize [LifecycleAware] or [MutableLifecycleAware].
 * @see LifecycleAware
 * @see MutableLifecycleAware
 * */
fun <T : Any> Fragment.viewLifecycleAware(creator: () -> T): ReadOnlyProperty<LifecycleOwner, T> =
    LifecycleAware(creator, viewLifecycleOwner)

/**
 * Provides common way to initialize [LifecycleAware] or [MutableLifecycleAware].
 * @see LifecycleAware
 * @see MutableLifecycleAware
 * */
fun <T : Any> Fragment.lifecycleAware(default: T? = null): ReadWriteProperty<LifecycleOwner, T> =
    MutableLifecycleAware(default, viewLifecycleOwner)

// ---

/**
 * Marks this [KProperty0] accessible temporarily and retrieves its delegate. It checks whether
 * this delegate conforms to [MutableLifecycleAware] type and returns the value. If for whatever
 * reason this field doesn't contain the delegate or the delegate is erased then the result is
 * null.
 *
 * After the delegate's been retrieved, the field is marked accessible as per its initial state.
 *
 * @return instance of current [MutableLifecycleAware] or null
 * */
fun <T : Any> KProperty0<T>.getLifecycleAwareDelegate(): MutableLifecycleAware<T>? {
    val wasAccessible = isAccessible
    isAccessible = true
    val delegate = getDelegate() as? MutableLifecycleAware<T>
    isAccessible = wasAccessible
    return delegate
}

/**
 * Behaves in the similar to [getLifecycleAwareDelegate], but throws [IllegalArgumentException]
 * if the [MutableLifecycleAware] is null or cannot be retrieved
 *
 * @see getLifecycleAwareDelegate
 * @return instance of current [MutableLifecycleAware]
 * @throws IllegalArgumentException when [MutableLifecycleAware] cannot be found
 * */
fun <T : Any> KProperty0<T>.requireLifecycleAwareDelegate() =
    requireNotNull(getLifecycleAwareDelegate())
