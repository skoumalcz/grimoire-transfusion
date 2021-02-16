package com.skoumal.grimoire.transfusion.environment.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class EnvironmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : View(context, attrs, style) {

    abstract class Named {

        abstract val name: String

    }

    // region vars
    var state: Named? = null
        set(value) {
            field = value
            textView.text = value?.name
        }
    // endregion

    // region views
    private val textView = TextView(context, attrs, style)
    // endregion

}