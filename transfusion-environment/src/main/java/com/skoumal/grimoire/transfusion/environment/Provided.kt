package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.flow.Flow

interface Provided<T> {

    fun flow(): Flow<T>

}