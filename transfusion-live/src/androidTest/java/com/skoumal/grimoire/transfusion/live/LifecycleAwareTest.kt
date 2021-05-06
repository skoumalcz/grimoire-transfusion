package com.skoumal.grimoire.transfusion.live

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random.Default.nextBytes
import kotlin.random.Random.Default.nextInt

@SmallTest
@RunWith(AndroidJUnit4::class)
class LifecycleAwareTest {

    private lateinit var value: String
    private lateinit var creator: () -> String
    private lateinit var owner: TestLifecycleOwner
    private lateinit var observer: LifecycleAware<String>

    @Before
    fun prepare() {
        value = String(nextBytes(nextInt(0, 100)))
        creator = { value }
        owner = TestLifecycleOwner()
        observer = LifecycleAware(creator = creator, registrar = TestLifecycleRegistrar())
    }

    @After
    fun tearDown() {
        owner.lifecycle.removeObserver(observer)
    }

    // ---

    @Test
    fun initializes() {
        val value = observer.getValue(owner, this::observer)
        assert(value === this.value) {
            "Value was created with new string instance"
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun destroysSelfWithLifecycle() {
        // initializes the contents
        var value = observer.getValue(owner, this::observer)
        assert(value === this.value) { "Value was not initialized" }

        owner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        value = observer.getValue(owner, this::observer) // should throw
        @Suppress("SENSELESS_COMPARISON")
        assert(value != null) { "Value was not nulled" }
        assert(false) { "ReadOnlyProperty is not read protected after destroy lifecycle event" }
    }

}