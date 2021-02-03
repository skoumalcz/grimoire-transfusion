package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.flow.first

suspend fun <T> Provided<T>.value() =
    flow().first()

inline fun <T, R> Provided<T>.map(
    cached: Boolean = true,
    crossinline keySelector: (T) -> Any = { it.hashCode() },
    crossinline mapper: (T) -> R
): Provided<R> = when {
    cached -> CachedProvidedValue(this, keySelector = { keySelector(it) }) { mapper(it) }
    else -> AnonymousProvidedValue(this) { mapper(it) }
}
