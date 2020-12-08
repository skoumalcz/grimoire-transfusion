package com.skoumal.grimoire.transfusion

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

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

    private val viewEvents = BroadcastChannel<E>(Channel.BUFFERED)

    override fun openFlow(): Flow<E> {
        return viewEvents.openSubscription().consumeAsFlow()
    }

    override fun offerToFlow(element: E): Boolean {
        return viewEvents.offer(element)
    }

    override fun disposeFlow() {
        viewEvents.close()
    }

}