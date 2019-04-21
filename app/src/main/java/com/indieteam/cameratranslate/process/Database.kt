package com.indieteam.cameratranslate.process

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class Database(context: Context): SQLiteOpenHelper(context, "CameraTranslate", null, 1){
    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "CREATE TABLE IF NOT EXISTS translated ( " +
                "word_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "word_raw text," +
                "word_translated text)"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}