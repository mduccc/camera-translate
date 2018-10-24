package com.indieteam.cameratranslate.process

import android.content.Context
import android.util.Log
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.ResultActivity
import kotlinx.android.synthetic.main.activity_cam2_real_time.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class CloudTranslate{

    private val client = OkHttpClient()
    private val apiAddress = ApiAddress()
    private lateinit var context: Context
    private lateinit var mode: String
    private var lastTranslated = ""
    private var lastInput = ""

    fun init(context: Context, mode: String){
        this.context = context
        this.mode = mode
    }

    fun toVietnamese(input: String){
        object : Thread() {
            override fun run() {
                if (lastInput == input) {
                    (context as Cam2RealTimeActivity).apply {
                        runOnUiThread {
                            this.text_translated.text = lastTranslated
                        }
                        frame = 0
                    }
                } else {
                    lastInput = input
                    try {
                        val request = Request.Builder()
                                .url(apiAddress.url_request(input))
                                .build()
                        val response = client.newCall(request).execute()
                        if (response.body() != null) {
                            val bodyObj = JSONObject(response.body()!!.string())
                            if (bodyObj.getString("code") == "200") {
                                val resultArr = bodyObj.getJSONArray("text")
                                var translated = ""
                                for (i in 0 until resultArr.length()) {
                                    translated += resultArr[i]
                                }
                                Log.d("translated", translated)
                                translated = translated.toLowerCase()
                                lastTranslated = translated
                                if (mode == "RealTime") {
                                    if (translated.isNotBlank()) {
                                        (context as Cam2RealTimeActivity).apply {
                                            runOnUiThread {
                                                this.text_translated.text = translated
                                            }
                                            frame = 0
                                        }
                                    }
                                } else {
                                    if (translated.isNotBlank()) {
                                        (context as ResultActivity).apply {
                                            runOnUiThread {
                                                this.text_translated.text = translated
                                            }
                                        }
                                    }
                                }
                                Log.d("Translate result", translated)
                            } else Log.d("code", bodyObj.getString("code"))
                        } else Log.d("response", "null")

                    } catch (e: Exception) {
                        Log.d("Logic Err", e.toString())
                    }
            }
            }
        }.start()
    }

}