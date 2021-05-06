package com.skoumal.grimoire.transfusion.method.consumer

import androidx.fragment.app.Fragment
import com.skoumal.grimoire.transfusion.method.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Deprecated("Use TransfusionLifecycleConsumer")
internal class FragmentTransfusionFlowConsumer<C : Cell>(
    private val fragment: Fragment,
    scope: CoroutineScope
) : OrderedTransfusionFlowConsumer<C>, CoroutineScope by scope {

    private var job: Job? = null

    override fun digest(flow: Flow<C>) {
        job?.cancel()
        job = launch { flow.consume() }
    }

    override fun stop() {
        job?.cancel()
    }

    // ---

    private suspend fun Flow<C>.consume() = collect { executableCell ->
        executableCell
            .consumeIfInstanceSealed<InFragment> { it(fragment) }
            .consumeIfInstanceSealed<InActivity> { it(fragment.requireActivity()) }
            .consumeIfInstanceSealed<InContext> { it(fragment.requireContext().applicationContext) }
    }

}