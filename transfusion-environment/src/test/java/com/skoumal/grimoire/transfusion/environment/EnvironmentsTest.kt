@file:Suppress("CanSealedSubClassBeObject")

package com.skoumal.grimoire.transfusion.environment

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EnvironmentsTest {

    @Before
    fun setUp() {
        Environments.isInstantiated = false
    }

    // ---

    @Test
    fun `environments can be consumed multiple times`() {
        val staging = Env.Staging()
        val envs = envs

        envs[Type.Staging] = lazy { staging }

        runBlockingTest {
            envs.apply(Type.Staging)
            var env: Env? = envs.current()

            assertThat(env).isNotNull()
            assertThat(env).isSameInstanceAs(staging)

            env = envs.current()

            assertThat(env).isNotNull()
            assertThat(env).isSameInstanceAs(staging)
        }
    }

    @Test
    fun `environments are dispatched via infinite flows`() {
        val prod = Env.Prod()
        val staging = Env.Staging()
        val envs = envs

        runBlockingTest {
            envs[Type.Staging] = lazy { staging }
            envs[Type.Prod] = lazy { prod }

            var appliedEnv: Env = staging
            var visitedTimes = 0
            envs.apply(Type.Staging)
            envs.asFlow().collectLatest {
                visitedTimes++
                assertThat(it).isSameInstanceAs(appliedEnv)

                if (it is Env.Staging) {
                    assertThat(visitedTimes).isEqualTo(1)
                    appliedEnv = prod
                    envs.apply(Type.Prod)
                } else if (it is Env.Prod) {
                    assertThat(visitedTimes).isEqualTo(2)
                    envs.stream.close()
                }
            }
        }
    }


    @Test
    fun `removed environments cannot be activated`() {
        val envs = envs
        envs[Type.Staging] = lazy { Env.Staging() }

        runBlockingTest {
            envs.remove(Type.Staging)
            try {
                envs.apply(Type.Staging)
                assert(false)
            } catch (ok: Throwable) {
            }
        }
    }

    @Test
    fun `removed environments' keys are not present`() {
        val envs = envs
        envs[Type.Staging] = lazy { Env.Staging() }
        envs.remove(Type.Staging)

        assertThat(envs.keys()).doesNotContain(Type.Staging)
    }

    @Test
    fun `removed existing environments via reference are removed`() {
        val env = Env.Staging()
        val envs = envs

        envs[Type.Staging] = lazy { env }
        assertThat(envs.keys()).isNotEmpty()

        envs.remove(env)
        assertThat(envs.keys()).isEmpty()
    }


    @Test
    fun `added environments can be activated`() {
        val staging = Env.Staging()
        val envs = envs

        envs[Type.Staging] = lazy { staging }

        runBlockingTest {
            envs.apply(Type.Staging)
            val env = envs.current()

            assertThat(env).isNotNull()
            assertThat(env).isSameInstanceAs(staging)
        }
    }

    @Test
    fun `added environments do not influence stream`() {
        val envs = envs
        envs[Type.Initial] = lazy { Env.Initial }
        envs.offer(Type.Initial)
        runBlockingTest {
            assertThat(envs.current()).isSameInstanceAs(Env.Initial)

            envs[Type.Staging] = lazy { Env.Staging() }

            assertThat(envs.current()).isSameInstanceAs(Env.Initial)
        }
    }


    // ---


    enum class Type {
        Staging, Prod, Initial
    }

    sealed class Env : Environment {

        class Staging : Env()
        class Prod : Env()
        object Initial : Env()

    }

    val envs get() = Environments<Type, Env>()


}