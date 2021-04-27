package com.skoumal.grimoire.transfusion.live

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
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
class MutableLifecycleAwareTest {

    private lateinit var initialValue: String
    private lateinit var owner: TestLifecycleOwner
    private lateinit var observer: MutableLifecycleAware<String>

    @Before
    fun prepare() {
        initialValue = String(nextBytes(nextInt(1, 100)))
        owner = TestLifecycleOwner()
        observer = MutableLifecycleAware(default = initialValue)
    }

    @After
    fun tearDown() {
        owner.lifecycle.removeObserver(observer)
    }

    // ---

    @Test
    fun initializes() {
        val value = observer.getValue(owner, this::observer)
        assert(value === this.initialValue) {
            "Value was created with new string instance"
        }
    }

    @Test
    fun assignsNewValue() {
        val newValue = String(nextBytes(nextInt(1, 100)))
        observer.setValue(owner, this::observer, newValue)
        val fetchedValue = observer.getValue(owner, this::observer)
        assert(newValue === fetchedValue) {
            "Values are not equal"
        }
    }

    @Test(expected = IllegalStateException::class)
    fun requiresInitialValue() {
        val empty = MutableLifecycleAware<String>()
        empty.getValue(owner, this::observer)
        assert(false) {
            "Value should require default value"
        }
    }

    @Test
    fun checkHasNoValue() {
        val tested = object : LifecycleOwner by owner {
            var prop by MutableLifecycleAware<String>()
        }
        val propIsNotNull = tested::prop.requireLifecycleAwareDelegate().isNotNull
        assert(!propIsNotNull) {
            "Initialized property is not null"
        }
    }

    @Test
    fun checkHasValue() {
        val tested = object : LifecycleOwner by owner {
            var prop by observer
        }
        val propIsNotNull = tested::prop.requireLifecycleAwareDelegate().isNotNull
        assert(propIsNotNull) {
            "Initialized property is null"
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun destroysSelfWithLifecycle() {
        // initializes the contents
        var value = observer.getValue(owner, this::observer)
        assert(value === this.initialValue) { "Value was not initialized" }

        owner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        assert(!observer.isNotNull) { "Observer did not clear its value" }

        value = observer.getValue(owner, this::observer) // should throw
        @Suppress("SENSELESS_COMPARISON")
        assert(value != null) { "Value was not nulled" }
        assert(false) { "ReadOnlyProperty is not read protected after destroy lifecycle event" }
    }

}