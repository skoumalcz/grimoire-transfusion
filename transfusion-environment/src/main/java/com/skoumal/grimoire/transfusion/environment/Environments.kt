@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.skoumal.grimoire.transfusion.environment

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow

private typealias K = Any
private typealias E = Environment

class Environments<Key : K, Env : E> {

    internal val stream = BroadcastChannel<Env>(Channel.CONFLATED)
    internal val available = hashMapOf<Key, Lazy<Env>>()

    init {
        require(!isInstantiated) { "Cannot create more then one set of environments!" }
        isInstantiated = true
    }

    companion object {

        @Volatile
        internal var isInstantiated: Boolean = false

    }

}

// region Attaching environments

operator fun <Key : K, Env : E> Environments<Key, Env>.set(key: Key, env: Lazy<Env>) {
    available[key] = env
}

operator fun <Key : K, Env : E> Environments<Key, Env>.set(key: Key, env: () -> Env) =
    set(key, lazy(env))

internal fun <Key : K, Env : E> Environments<Key, Env>.remove(env: Env) = available
    .filter { it.value.value === env }
    .forEach { remove(it.key) }

fun <Key : K, Env : E> Environments<Key, Env>.remove(tag: Key) =
    available.remove(tag)

// endregion

// region Applying environments

internal suspend fun <Key : K, Env : E> Environments<Key, Env>.apply(env: Env) {
    stream.send(env)
}

suspend fun <Key : K, Env : E> Environments<Key, Env>.apply(key: Key) =
    apply(available.getValue(key).value)

internal fun <Key : K, Env : E> Environments<Key, Env>.offer(env: Env) =
    stream.offer(env)

fun <Key : K, Env : E> Environments<Key, Env>.offer(key: Key) =
    offer(available.getValue(key).value)

// endregion

// region Fetching environments

fun <Key : K, Env : E> Environments<Key, Env>.asFlow() =
    stream.openSubscription().receiveAsFlow()

suspend fun <Key : K, Env : E> Environments<Key, Env>.current() =
    asFlow().firstOrNull()

fun <Key : K, Env : E> Environments<Key, Env>.keys(): Set<Key> =
    available.keys

// endregion
