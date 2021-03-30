package com.skoumal.grimoire.transfusion

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import kotlin.properties.Delegates

/**
 * Interface that allows user to be observed via DataBinding or manually by assigning listeners.
 * It is designed to be implemented by your [androidx.lifecycle.ViewModel]. Other usages are
 * permitted, but internally untested.
 *
 * @see [androidx.databinding.Observable]
 * */
interface Observer : Observable {

    /**
     * Notifies all observers that something has changed. By default implementation this method is
     * synchronous, hence observers will never be notified in undefined order. Observers might
     * choose to refresh the view completely, which is beyond the scope of this function.
     * */
    fun notifyChange(sender: Observable)

    /**
     * Notifies all observers about field with [fieldId] has been changed. This will happen
     * synchronously before or after [notifyChange] has been called. It will never be called during
     * the execution of aforementioned method.
     * */
    fun notifyPropertyChanged(sender: Observable, fieldId: Int)

    /**
     * Clears all observers from this object unconditionally. It should only be called right after
     * lifecycle switches to DESTROYED. Any other state could produce undesired effects.
     *
     * If no callback was registered before, it does nothing.
     * */
    fun clear()

    companion object {

        fun getDefault(): Observer = DefaultObserver()

    }

}

private class DefaultObserver : Observer {

    @Transient
    private var callbacks: PropertyChangeRegistry? = null

    @Synchronized
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        if (callbacks == null) {
            callbacks = PropertyChangeRegistry()
        }
        callbacks?.add(callback)
    }

    @Synchronized
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks?.remove(callback)
    }

    @Synchronized
    override fun notifyChange(sender: Observable) {
        callbacks?.notifyCallbacks(sender, 0, null)
    }

    @Synchronized
    override fun notifyPropertyChanged(sender: Observable, fieldId: Int) {
        callbacks?.notifyCallbacks(sender, fieldId, null)
    }

    @Synchronized
    override fun clear() {
        callbacks?.clear()
    }
}

// region Extensions

/**
 * Declares delegated property in [Observer] parent. This property is available for DataBinding
 * to be observed as usual. The only caveat is that in order for binding to generate the [fieldIds]
 * it has to be annotated accordingly.
 *
 * Either ways you can completely omit the [fieldIds] and the delegate will automatically invoke
 * [Observer.notifyChange]. Be very careful, as this will refresh all fields.
 *
 * The annotation however give very strict control over your internal fields and overall reduce
 * overhead in notifying observers. (In comparison to [androidx.databinding.ObservableField])
 * It helps the kotlin code to feel more,... _native_, while respecting the original functionality.
 *
 * # Examples:
 *
 * ## The most basic usage would probably be:
 * ```kotlin
 * @get:Bindable
 * var myField by observable(BR.myField)
 *      private set
 * ```
 *
 * ## You can use the field as public read/write, of course:
 * ```kotlin
 * @get:Bindable
 * @set:Bindable
 * var myField by observable(BR.myField)
 * ```
 *
 * ## Please beware that delegated property instantiates one class per property
 * We discourage using simple getters via delegated properties. Instead you can do something like
 * this:
 *
 * ```kotlin
 * @get:Bindable
 * @set:Bindable
 * var myField by observable(defaultValue, BR.myField, BR.myTransformedField)
 *
 * var myTransformedField: TransformationResult
 *      @Bindable get() {
 *          return myField.transform()
 *      }
 * ```
 *
 * */
fun <T> Observer.observable(
    initialValue: T,
    vararg fieldIds: Int,
    afterChanged: ((T) -> Unit)? = null
) = Delegates.observable(initialValue) { _, old, value ->
    if (old != value) {
        if (fieldIds.isEmpty()) {
            notifyChange(this)
        } else {
            fieldIds.forEach { notifyPropertyChanged(this, it) }
        }
        afterChanged?.invoke(value)
    }
}

// endregion