package com.skoumal.grimoire.transfusion

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * Interface allowing unidirectional communication from point A to point B.
 *
 * You can use default implementation like so:
 *
 * ```kotlin
 * class MyViewModel: ViewModel(), Transfusion<Cell> by Transfusion.getDefault()
 * ```
 *
 * @see com.skoumal.grimoire.transfusion.method.consumer.transfusion for the reference on how to
 * consume the flows correctly
 * */
@OptIn(ExperimentalCoroutinesApi::class)
interface Transfusion<E> {

    /**
     * Opens a flow to _this_ transfusion. Provider must return a flow that's ready to be
     * subscribed to. Consumer doesn't have to check whether the flow is ready or not, its state is
     * taken for granted and consumed _as-is_.
     * */
    fun openFlow(): Flow<E>

    /**
     * Offers element to the internal implementation. Element might be rejected and in that case
     * returns `false`. In other case the implementation is expected to return true whenever
     * [element] gets successfully queued for consumption.
     * */
    fun offerToFlow(element: E): Boolean

    /**
     * Subscriber might choose to completely dispose of the flow. In which case provider is
     * required to close the current flow.
     *
     * Be aware that, depending on the implementation, [openFlow] might either throw exceptions or
     * return the data according to the docs.
     * */
    fun disposeFlow()

    @Deprecated(
        "Use method offer instead. Publish doesn't really represent what the transfusion stands for.",
        ReplaceWith("offer")
    )
    fun E.publish() = offer()

    /**
     * Convenience method to offer to the flow. Implementation is identical to [offerToFlow]
     * except doesn't return the status.
     *
     * This method might be beneficial for inlining methods, such as:
     * ```
     * fun onNavigateToScreen()/*: Unit*/ = NavigationEvent().offer()
     * ```
     *
     * @see offerToFlow
     * */
    fun E.offer() {
        offerToFlow(this)
    }

    companion object {

        /**
         * Creates and returns a default implementation of transfusion.
         *
         * Backing, through which are events dispatched, is buffered by its nature to never miss
         * any events. This means that dispatching events _before_ the observer subscribes results
         * in subscriber receiving multiple events at once and then subsequently waiting for more.
         * */
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