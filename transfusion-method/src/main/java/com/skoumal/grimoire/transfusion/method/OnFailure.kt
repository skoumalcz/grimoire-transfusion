package com.skoumal.grimoire.transfusion.method

interface OnFailure {

    /**
     * [onFailure] is called whenever parameter of parent executor cannot be resolved or exception
     * was raised during execution of parent executor. This is important so using events on
     * unsupported platform doesn't induce crashes.
     * */
    fun onFailure(throwable: Throwable) = throwable.printStackTrace()

}