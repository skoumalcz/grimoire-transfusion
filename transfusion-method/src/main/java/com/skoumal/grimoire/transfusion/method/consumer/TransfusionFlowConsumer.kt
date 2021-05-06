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
@Deprecated("Use automatically managed TransfusionLifecycleConsumer via LifecycleOwner.transfusion {}. This API will be removed in 2.0.0")
@Suppress("FunctionName", "DeprecatedCallableAddReplaceWith")
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
@Deprecated("Use automatically managed TransfusionLifecycleConsumer via LifecycleOwner.transfusion {}. This API will be removed in 2.0.0")
@Suppress("FunctionName", "DeprecatedCallableAddReplaceWith")
fun <C : Cell> Flow<C>.consumeIn(
    fragment: Fragment
): TransfusionFlowConsumer<C> = FragmentTransfusionFlowConsumer<C>(
    fragment = fragment,
    scope = fragment.lifecycleScope
).also {
    it.digest(this)
}

@Deprecated("Use automatically managed TransfusionLifecycleConsumer via LifecycleOwner.transfusion {}. This API will be removed in 2.0.0")
internal interface OrderedTransfusionFlowConsumer<C : Cell> : TransfusionFlowConsumer<C> {

    fun digest(flow: Flow<C>)

}

@Deprecated("Use automatically managed TransfusionLifecycleConsumer via LifecycleOwner.transfusion {}. This API will be removed in 2.0.0")
interface TransfusionFlowConsumer<C : Cell> {

    @Deprecated("Use automatically managed TransfusionLifecycleConsumer via LifecycleOwner.transfusion {}. This API will be removed in 2.0.0")
    fun stop()

}