package com.skoumal.grimoire.transfusion.environment

import java.lang.ref.WeakReference

interface Cached<Key, Value> {

    fun getCached(key: Key): Value?
    fun putCached(key: Key, value: Value)

    companion object {

        fun <K, V> default(): Cached<K, V> {
            return DefaultCached()
        }

    }

}

private class DefaultCached<K, V> : Cached<K, V> {

    private val values = mutableMapOf<K, WeakReference<V>>()

    @Synchronized
    override fun getCached(key: K): V? {
        return values[key]?.get()
    }

    @Synchronized
    override fun putCached(key: K, value: V) {
        values[key] = WeakReference(value)
    }

}