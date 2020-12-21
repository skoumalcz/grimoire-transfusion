package com.skoumal.grimoire.transfusion.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun interface TransfusionObserver<T> : Observer<T>

/**
 * Host allowing managed consumption of [LiveData] inside non-lifecycle scope. By using this you're
 * responsible for clearing the listeners as the whole object is cleared - that be with the usage of
 * [AutoCloseable] or in a ViewModel's [onCleared][androidx.lifecycle.ViewModel.onCleared].
 *
 * You can use default implementation like so:
 *
 * ```kotlin
 * class MyViewModel: ViewModel()
 * ```
 * */
interface LiveTransfusionHost {

    /**
     * Starts a consumption on provided LiveData. Be wary that you absolutely **have to**
     * [clearTransfusions] if you want to use LiveData inside non-lifecycle scope.
     *
     * @return Observer, that doesn't need to be saved or cached as the default implementation
     * implements this functionality for you.
     * */
    fun <T> LiveData<T>.transfusion(observer: TransfusionObserver<T>): Observer<T>

    /** Clears all listeners and destroys references to all transfusions */
    fun clearTransfusions()

    companion object {

        /**
         * Creates and returns default implementation. All transfusions are saved internally and
         * cleared completely when calling [clearTransfusions].
         * */
        fun getDefault(): LiveTransfusionHost = DefaultLiveTransfusionHost()

    }

}

private class DefaultLiveTransfusionHost : LiveTransfusionHost {

    @Volatile
    private var transfusions: HashSet<TransfusionHolder<*>>? = null

    override fun <T> LiveData<T>.transfusion(observer: TransfusionObserver<T>) = observer.also {
        observeForever(it)
        provideRegistry().add(TransfusionHolder(this, observer))
    }

    @Synchronized
    override fun clearTransfusions() {
        with(provideRegistry()) {
            forEach { it.clear() }
            clear()
        }
        transfusions = null
    }

    @Synchronized
    private fun provideRegistry(): HashSet<TransfusionHolder<*>> {
        if (transfusions == null) {
            transfusions = hashSetOf()
        }
        return transfusions!!
    }

    private class TransfusionHolder<T>(
        @JvmField private val observable: LiveData<T>,
        @JvmField private val observer: Observer<T>
    ) {

        fun clear() = observable.removeObserver(observer)

    }

}
