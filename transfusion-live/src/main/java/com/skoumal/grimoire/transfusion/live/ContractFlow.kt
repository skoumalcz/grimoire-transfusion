package com.skoumal.grimoire.transfusion.live

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class ContractFlow<I, O> internal constructor(
    flow: Flow<O>,
    private val launcher: ActivityResultLauncher<I>
) : Flow<O> by flow {

    fun launch(input: I) = launcher.launch(input)

}

fun <I, O> ComponentActivity.contract(contract: ActivityResultContract<I, O>): ContractFlow<I, O?> {
    val channel = Channel<O?>()
    val launcher = registerForActivityResult(contract) {
        channel.offer(it)
    }

    return contract(channel, launcher)
}

fun <I, O> Fragment.contract(contract: ActivityResultContract<I, O>): ContractFlow<I, O?> {
    val channel = Channel<O?>()
    val launcher = registerForActivityResult(contract) {
        channel.offer(it)
    }

    return contract(channel, launcher)
}

private fun <I, O> LifecycleOwner.contract(
    channel: Channel<O?>,
    launcher: ActivityResultLauncher<I>
): ContractFlow<I, O?> {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            launcher.unregister()
        }
    })

    return ContractFlow(channel.receiveAsFlow(), launcher)
}