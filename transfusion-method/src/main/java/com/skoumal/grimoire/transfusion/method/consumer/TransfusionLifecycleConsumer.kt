package com.skoumal.grimoire.transfusion.method.consumer

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.skoumal.grimoire.transfusion.Transfusion
import com.skoumal.grimoire.transfusion.method.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TransfusionLifecycleConsumer<E : Cell> internal constructor(
    private val creator: () -> Transfusion<E>
) : LifecycleObserver {

    private var job: Job? = null
    private var owner: LifecycleOwner? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(owner: LifecycleOwner) {
        this.owner = owner
        this.job = owner.lifecycleScope.launch {
            creator().openFlow().collect {
                consumeEvent(it)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        this.job?.cancel()
        this.job = null
        this.owner = null
    }

    // ---

    private fun consumeEvent(event: E) {
        val owner = owner
            ?: return println("LifecycleOwner is null, event $event is ignored")

        val result = when (owner) {
            is Fragment -> consumeInFragment(owner, event)
            is ComponentActivity -> consumeInActivity(owner, event)
            is Activity -> return println("Your activity does not extend ComponentActivity! Event $event is ignored")
            else -> return println("Unsupported LifecycleOwner found! Report this issue to the maintainer's repository.")
        }

        if (!result.isConsumed) {
            println("Event $event was not consumed by any source! Make sure that your Cell extends InFragment, InActivity or InContext!")
        }
    }

    private fun consumeInFragment(owner: Fragment, event: E) = event
        .consumeIfInstanceSealed<InFragment> { it(owner) }
        .consumeIfInstanceSealed<InActivity> { it(owner.requireActivity()) }
        .consumeIfInstanceSealed<InContext> { it(owner.requireContext().applicationContext) }

    private fun consumeInActivity(owner: ComponentActivity, event: E) = event
        .consumeIfInstanceSealed<InActivity> { it(owner) }
        .consumeIfInstanceSealed<InContext> { it(owner.applicationContext) }

}


// ---


/**
 * Creates and returns instance of lifecycle aware component that consumes all events from provided
 * [creator] that implement [InFragment], [InActivity] or [InContext] in this order. [creator] is
 * called every time lifecycle reaches [Lifecycle.Event.ON_CREATE] and created references are
 * cleared when [Lifecycle.Event.ON_DESTROY]ed.
 *
 * Depending on your implementation of [LifecycleOwner] this should be safe to call on init as it
 * hooks into [Lifecycle] and starts execution only in aforementioned circumstances.
 *
 * example:
 *
 * ```kotlin
 * class MyFragment : Fragment(R.layout.fragment_my) {
 *
 *     private val viewModel: MyViewModel by viewModels() // implements Transfusion<Cell>
 *
 *     init {
 *         transfusion { viewModel }
 *     }
 *
 * }
 * ```
 *
 *
 * */
fun <E : Cell> LifecycleOwner.transfusion(
    creator: () -> Transfusion<E>
) = TransfusionLifecycleConsumer(creator).apply {
    lifecycle.addObserver(this)
}
