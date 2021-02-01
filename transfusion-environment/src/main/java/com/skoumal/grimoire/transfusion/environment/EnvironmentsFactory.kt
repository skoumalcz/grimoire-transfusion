package com.skoumal.grimoire.transfusion.environment

class EnvironmentsFactory<Key : Any, Env : Environment> {

    private val envs = hashMapOf<Key, Lazy<Env>>()
    private var active: Key? = null

    fun addEnvironment(key: Key, env: Lazy<Env>) = apply {
        envs[key] = env
    }

    fun setActive(key: Key) = apply {
        this.active = key
    }

    fun build(): Environments<Key, Env> {
        require(envs.isNotEmpty())
        val active = active
        val environments = Environments<Key, Env>()

        envs.forEach { entry ->
            environments[entry.key] = entry.value
        }

        if (active != null) {
            environments.offer(active)
        }

        return environments
    }

}

fun <Key : Any, Env : Environment> EnvironmentsFactory<Key, Env>.addEnvironment(
    key: Key,
    body: () -> Env
) = apply {
    addEnvironment(key, lazy(body))
}