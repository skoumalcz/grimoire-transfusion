package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.flow.first

suspend fun <T> Provided<T>.value() =
    flow().first()

inline fun <T, R> Provided<T>.map(crossinline mapper: (T) -> R): Provided<R> =
    AnonymousProvidedValue(this) { mapper(it) }
