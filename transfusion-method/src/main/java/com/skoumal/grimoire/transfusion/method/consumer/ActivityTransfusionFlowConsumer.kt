package com.skoumal.grimoire.transfusion.method.consumer

import androidx.activity.ComponentActivity
import com.skoumal.grimoire.transfusion.method.Cell
import com.skoumal.grimoire.transfusion.method.InActivity
import com.skoumal.grimoire.transfusion.method.InContext
import com.skoumal.grimoire.transfusion.method.consumeIfInstanceSealed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class ActivityTransfusionFlowConsumer<C : Cell>(
    private val activity: ComponentActivity,
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
            .consumeIfInstanceSealed<InActivity> { it(activity) }
            .consumeIfInstanceSealed<InContext> { it(activity.applicationContext) }
    }

}