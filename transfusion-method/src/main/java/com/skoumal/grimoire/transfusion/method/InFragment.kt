package com.skoumal.grimoire.transfusion.method

import androidx.fragment.app.Fragment

/** @see InContext */
interface InFragment : OnFailure {

    /** @see [InContext.invoke] */
    operator fun invoke(fragment: Fragment)

}