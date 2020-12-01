package com.skoumal.grimoire.transfusion.method

import com.skoumal.grimoire.talisman.seal.onFailure
import com.skoumal.grimoire.talisman.seal.onSuccess
import com.skoumal.grimoire.talisman.seal.runSealed

/**
 * Class for passing events from ViewModels to Activities/Fragments
 * Variable [isConsumed] used so each event is consumed only once
 */
abstract class Cell {

    var isConsumed = false
        private set

    fun consume() {
        isConsumed = true
    }

}

/**
 * Consumes the [Cell] if is instance of given [T] and executes [body].
 *
 * The [body] cannot be logically called when is the aforementioned condition `false`. It won't be
 * however called if the ViewEvent has been previously consumed. Additionally if the [body] fails
 * to complete its action normally (throws) it **WILL NOT** be marked as consumed.
 * */
inline fun <reified T> Cell.consumeIfInstanceSealed(body: (T) -> Unit) = apply {
    if (isConsumed || this !is T) {
        return@apply
    }

    runSealed { body(this) }
        .onFailure(this::notifyOrThrow)
        .onSuccess { consume() }
}

fun Cell.notifyOrThrow(throwable: Throwable) {
    if (this !is OnFailure) {
        throw throwable
    }
    onFailure(throwable)
}
