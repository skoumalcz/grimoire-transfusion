package com.skoumal.grimoire.transfusion.environment

interface Environment {

    val name: String
    val level: Level

    enum class Level {
        Nightly, Staging, Stable
    }

}