package com.skoumal.grimoire.transfusion.method.consumer

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.skoumal.grimoire.transfusion.method.Cell
import kotlinx.coroutines.flow.Flow

/**
 * This method is only safe to call in [onCreate][ComponentActivity.onCreate]. It automatically
 * establishes all necessary backing to allow communication throughput via provided [Flow].
 * Provided [Flow] may not fail or close prematurely, consumption will not be resumed automatically.
 *
 * You are obligated to save this object and call [stop][TransfusionFlowConsumer.stop] when
 * framework calls [onDestroy][ComponentActivity.onDestroy].
 * */
@Suppress("FunctionName")
fun <C : Cell> Flow<C>.consumeIn(
    activity: ComponentActivity
): TransfusionFlowConsumer<C> = ActivityTransfusionFlowConsumer<C>(
    activity = activity,
    scope = activity.lifecycleScope
).also {
    it.digest(this)
}

/**
 * This method is only safe to call in [onViewCreated][Fragment.onViewCreated]. It automatically
 * establishes all necessary backing to allow communication throughput via provided [Flow].
 * Provided [Flow] may not fail or close prematurely, consumption will not be resumed automatically.
 *
 * You are obligated to save this object and call [stop][TransfusionFlowConsumer.stop] when
 * framework calls [onDestroyView][Fragment.onDestroyView].
 * */
@Suppress("FunctionName")
fun <C : Cell> Flow<C>.consumeIn(
    fragment: Fragment
): TransfusionFlowConsumer<C> = FragmentTransfusionFlowConsumer<C>(
    fragment = fragment,
    scope = fragment.lifecycleScope
).also {
    it.digest(this)
}

internal interface OrderedTransfusionFlowConsumer<C : Cell> : TransfusionFlowConsumer<C> {

    fun digest(flow: Flow<C>)

}

interface TransfusionFlowConsumer<C : Cell> {

    fun stop()

}