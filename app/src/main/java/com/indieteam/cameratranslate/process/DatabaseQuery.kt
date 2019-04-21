package com.indieteam.cameratranslate.process

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.indieteam.cameratranslate.collection.TranslateCollection
import java.lang.Exception

class DatabaseQuery(context: Context): Database(context), DatabaseAction {

    override fun readALL(): ArrayList<TranslateCollection> {
        val data = ArrayList<TranslateCollection>()
        val read = readableDatabase

        val sql = "SELECT * FROM translated ORDER BY word_id DESC"

        val cursor = read.rawQuery(sql, null)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {
            val word_id = cursor.getString(0).toInt()
            val word_raw = cursor.getString(1)
            val word_translate = cursor.getString(2)

            Log.d("read database", "$word_raw, $word_translate")
            data.add(TranslateCollection(word_id, word_raw, word_translate))
            cursor.moveToNext()
        }

        cursor.close()
        return data
    }

    override fun insert(word_raw: String, word_translated: String): Boolean {
        try {
            val write = writableDatabase
            val content = ContentValues()

            content.put("word_raw", word_raw)
            content.put("word_translated", word_translated)

            write.insert("translated", null, content)
            write.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun delete(id: Int) : Boolean{
        try {
            val delete = writableDatabase
            delete.delete("translated", "word_id=?", arrayOf("$id"))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun deleteAll(): Boolean {
        try {
            val delete = writableDatabase
            delete.delete("translated", null, null)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}