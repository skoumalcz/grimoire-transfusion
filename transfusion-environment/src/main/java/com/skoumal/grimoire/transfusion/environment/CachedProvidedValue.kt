package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CachedProvidedValue<T, R>(
    private val input: Provided<T>,
    private val keySelector: (T) -> Any = { it?.hashCode() ?: 0 },
    private val mapper: (T) -> R,
) : Provided<R>, Cached<Any, R> by Cached.default() {

    override fun flow(): Flow<R> {
        return input.flow().map {
            getOrPut(keySelector(it)) { mapper(it) }
        }
    }

}