package com.indieteam.cameratranslate.ui

interface OnDetect {
    fun onDetected(text: String)
    fun onTranslated(text: String)
}