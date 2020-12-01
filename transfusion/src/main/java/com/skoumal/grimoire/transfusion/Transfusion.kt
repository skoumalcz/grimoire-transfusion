package com.skoumal.grimoire.transfusion

import com.skoumal.grimoire.talisman.Vessel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalCoroutinesApi::class)
interface Transfusion<E> {

    fun openFlow(): Flow<E>

    fun offerToFlow(element: E): Boolean

    fun disposeFlow()

    fun E.publish() {
        offerToFlow(this)
    }

    companion object {

        fun <E> getDefault(): Transfusion<E> = DefaultTransfusion()

    }

}

@OptIn(ExperimentalCoroutinesApi::class)
private class DefaultTransfusion<E> : Transfusion<E> {

    private val viewEvents = Vessel<E>(Channel.BUFFERED)

    override fun openFlow(): Flow<E> {
        return viewEvents.dock()
    }

    override fun offerToFlow(element: E): Boolean {
        return viewEvents.sail(element)
    }

    override fun disposeFlow() {
        viewEvents.sink()
    }

}