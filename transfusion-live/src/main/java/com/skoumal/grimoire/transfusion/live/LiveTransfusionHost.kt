package com.skoumal.grimoire.transfusion.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun interface TransfusionObserver<T> : Observer<T>

interface LiveTransfusionHost {

    fun <T> LiveData<T>.transfusion(observer: TransfusionObserver<T>): Observer<T>

    fun clearTransfusions()

    companion object {

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
        private val observable: LiveData<T>,
        private val observer: Observer<T>
    ) {

        fun clear() = observable.removeObserver(observer)

    }

}
