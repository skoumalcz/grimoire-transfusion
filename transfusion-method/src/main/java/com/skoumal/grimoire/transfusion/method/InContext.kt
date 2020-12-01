package com.skoumal.grimoire.transfusion.method

import android.content.Context

/**
 * # Definition
 * Executor main function is to provide a self-handling solution for ViewEvents which are often
 * broadcast to unknowing activity/fragment and has to be implemented selectively. Implementing
 * this interface allows your event to take over the execution and allows the ViewEvent to be
 * called from anywhere, anytime.
 * */
interface InContext : OnFailure {

    /**
     * # Definition
     * Function is executed as it reaches its destination. [onFailure] will be called if parameter
     * is null or execution of [invoke] throws an exception.
     *
     * ## Warning
     * Do not take any assumptions on what the context is! Context provided to you should be always
     * a root application context. If you need a theme wrapped context use [InFragment] or
     * [InActivity].
     * */
    operator fun invoke(context: Context)

}