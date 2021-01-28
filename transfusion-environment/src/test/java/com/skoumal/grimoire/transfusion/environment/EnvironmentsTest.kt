package com.skoumal.grimoire.transfusion.environment

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class EnvironmentsTest {

    @Test
    fun `factory attaches all environments`() {
        val staging = Env.Staging(Any(), Any())
        val prod = Env.Production(Any(), Any())
        val envs = EnvironmentsFactory<EnvKey, Env>()
            .addEnvironment(EnvKey.Debug, staging)
            .addEnvironment(EnvKey.Alpha, staging)
            .addEnvironment(EnvKey.Beta, prod)
            .addEnvironment(EnvKey.Release, prod)
            .build()

        assertThat(envs.available).containsEntry(EnvKey.Debug, staging)
        assertThat(envs.available).containsEntry(EnvKey.Alpha, staging)
        assertThat(envs.available).containsEntry(EnvKey.Beta, prod)
        assertThat(envs.available).containsEntry(EnvKey.Release, prod)
    }

    @Test
    fun `factory assigns active environment`() {
        val prod = Env.Production(Any(), Any())
        val envs = EnvironmentsFactory<EnvKey, Env>()
            .setActive(EnvKey.Release)
            .addEnvironment(EnvKey.Release, prod)
            .build()

        assertThat(envs.available).containsEntry(EnvKey.Release, prod)

        runBlockingTest {
            assertThat(envs.current()).isSameInstanceAs(prod)
        }
    }

    @Test
    fun `factory requires at least one environment`() {
        try {
            EnvironmentsFactory<EnvKey, Env>()
                .build()
            assert(false)
        } catch (ok: Throwable) {
        }
    }

    @Test
    fun `factory requires active environment to be attached`() {
        try {
            EnvironmentsFactory<EnvKey, Env>()
                .setActive(EnvKey.Debug)
                .addEnvironment(EnvKey.Release, Env.Production(Any(), Any()))
                .build()
            assert(false)
        } catch (ok: Throwable) {
        }
    }


    private sealed class EnvKey {
        object Debug : EnvKey()
        object Alpha : EnvKey()
        object Beta : EnvKey()
        object Release : EnvKey()
    }

    private sealed class Env : Environment {

        class Staging(
            val database: Any,
            val preferences: Any
        ) : Env()

        class Production(
            val database: Any,
            val preferences: Any
        ) : Env()

    }

}