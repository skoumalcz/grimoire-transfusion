package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnonymousProvidedValue<T, R>(
    private val input: Provided<T>,
    private val mapper: (T) -> R
) : Provided<R> {
    override fun flow(): Flow<R> {
        return input.flow().map { mapper(it) }
    }
}