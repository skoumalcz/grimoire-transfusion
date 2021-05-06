package com.skoumal.grimoire.transfusion.live

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class TestLifecycleRegistrar : LifecycleRegistrar {
    override fun isReadPermitted(owner: LifecycleOwner): Boolean {
        return owner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    }

    override fun register(owner: LifecycleOwner, observer: LifecycleObserver) {
        owner.lifecycle.addObserver(observer)
    }

    override fun unregister(owner: LifecycleOwner, observer: LifecycleObserver) {
        owner.lifecycle.removeObserver(observer)
    }
}