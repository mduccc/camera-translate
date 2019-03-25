package com.indieteam.cameratranslate.process

import android.content.Context
import android.util.Log
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.OnDetect
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class CloudTranslate (private val onDetect: OnDetect) {

    private val client = OkHttpClient()
    private val apiAddress = Api()
    private lateinit var context: Context
    private var cache = HashMap<String, String>()

    fun init(context: Context) {
        this.context = context
    }

    fun toVietnamese(input: String) {
        onDetect.onDetected(input)

        if (cache.containsKey(input)) {
           onDetect.onTranslated(cache[input]!!)
        } else {
            try {

                var newInput = input
                while (newInput.indexOf("\n") != -1) {
                    newInput = newInput.replace("\n", "%20%0A")
                }

                val request = Request.Builder()
                        .url(apiAddress.url_request(newInput))
                        .build()

                val response = client.newCall(request).execute()

                if (response.body() != null) {
                    val bodyObj = JSONObject(response.body()!!.string())
                    if (bodyObj.getString("code") == "200") {
                        val resultArr = bodyObj.getJSONArray("text")
                        var translated = ""

                        for (i in 0 until resultArr.length()) {
                            translated += resultArr[i].toString()
                        }

                        translated = translated.toLowerCase()
                        cache[input] = translated

                        if (translated.isNotBlank()) {
                            Log.d("Translated", translated)
                            onDetect.onTranslated(translated)
                        }
                    } else {
                        Log.d("Code", bodyObj.getString("code"))
                    }
                } else {
                    Log.d("Response", "null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        next()
    }

    private fun next() {
        (context as Cam2RealTimeActivity).apply {
            runOnUiThread {
                textRecognizeInImage.catchImage()
            }
        }
    }
}