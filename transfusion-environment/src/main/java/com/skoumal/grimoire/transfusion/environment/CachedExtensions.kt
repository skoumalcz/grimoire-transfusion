package com.skoumal.grimoire.transfusion.environment

inline fun <K, V> Cached<K, V>.getOrPut(key: K, put: () -> V): V {
    return getCached(key) ?: put().also {
        putCached(key, it)
    }
}