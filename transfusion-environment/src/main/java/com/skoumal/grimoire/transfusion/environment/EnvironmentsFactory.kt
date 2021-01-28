package com.skoumal.grimoire.transfusion.environment

class EnvironmentsFactory<Key : Any, Env : Environment> {

    private val envs = hashMapOf<Key, Env>()
    private var active: Key? = null

    fun addEnvironment(key: Key, env: Env) = apply {
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