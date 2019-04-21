package com.indieteam.cameratranslate.process

import com.indieteam.cameratranslate.collection.TranslateCollection

interface DatabaseAction{
    fun insert(word_raw: String, word_translated: String): Boolean
    fun delete(id: Int): Boolean
    fun readALL(): ArrayList<TranslateCollection>
    fun deleteAll(): Boolean
}