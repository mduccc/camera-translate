package com.indieteam.cameratranslate.ui

interface OnDetect {
    fun onTranslate()
    fun onEmpty()
    fun onDetected(text: String)
    fun onTranslated(text: String)
    fun onAPILive()
    fun onAPIError()
}