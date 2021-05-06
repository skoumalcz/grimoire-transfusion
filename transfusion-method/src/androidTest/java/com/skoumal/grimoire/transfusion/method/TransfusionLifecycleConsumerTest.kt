package com.skoumal.grimoire.transfusion.method

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.skoumal.grimoire.transfusion.Transfusion
import com.skoumal.grimoire.transfusion.method.consumer.TransfusionLifecycleConsumer
import com.skoumal.grimoire.transfusion.method.consumer.transfusion
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class TransfusionLifecycleConsumerTest {

    private lateinit var cell: Cell
    private lateinit var owner: TestLifecycleOwner
    private lateinit var transfusion: TestTransfusion
    private lateinit var observer: TransfusionLifecycleConsumer<Cell>

    @Before
    fun prepare() {
        cell = TestCell()
        owner = TestLifecycleOwner()
        transfusion = TestTransfusion()
        observer = owner.run {
            transfusion { transfusion }
        }
    }

    @After
    fun tearDown() {
        owner.lifecycle.removeObserver(observer)
    }

    // ---

    @Test
    fun checkIsCreated() {
        owner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val internalJob: Job? = observer.getField("job")
        val internalOwner: LifecycleOwner? = observer.getField("owner")

        assert(internalJob != null) { "Job was not started during ON_CREATE transition" }
        assert(internalJob!!.isActive) { "Job is not active right after init" }

        assert(internalOwner != null) { "Lifecycle Owner instance is not retained by consumer" }
        assert(internalOwner === owner) { "Lifecycle Owner differs from provided owner" }
    }

    @Test
    fun checkIsCleared() {
        owner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        val internalJob: Job? = observer.getField("job")
        val internalOwner: LifecycleOwner? = observer.getField("owner")

        assert(internalJob == null) { "Job was not cleared" }
        assert(internalOwner == null) { "Lifecycle Owner was not cleared" }
    }

    // ---

    private class TestTransfusion : Transfusion<Cell> by Transfusion.getDefault() {

        fun send(cell: Cell) = cell.offer()

    }

    private class TestCell : Cell()


}

private inline fun <reified T : Any, R> T.getField(name: String): R {
    return this::class.java.getDeclaredField(name).run {
        isAccessible = true
        get(this@getField) as R
    }
}