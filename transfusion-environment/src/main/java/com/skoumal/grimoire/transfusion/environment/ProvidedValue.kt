@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class ProvidedValue<T : Any>(
    initialValue: T? = null
) : Provided<T> {

    private val channel = BroadcastChannel<T>(Channel.CONFLATED)

    init {
        if (initialValue != null) {
            apply(initialValue)
        }
    }

    override fun flow(): Flow<T> =
        channel.openSubscription().receiveAsFlow()

    fun apply(value: T) =
        channel.offer(value)

}