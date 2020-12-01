package com.skoumal.grimoire.transfusion.method

import androidx.activity.ComponentActivity

/** @see InContext */
interface InActivity : OnFailure {

    /** @see [InContext.invoke] */
    operator fun invoke(activity: ComponentActivity)

}