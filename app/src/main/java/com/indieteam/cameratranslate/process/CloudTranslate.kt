package com.indieteam.cameratranslate.process

import android.content.Context
import android.util.Log
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.OnDetect
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress


class CloudTranslate (private val onDetect: OnDetect) {

    private val client = OkHttpClient()
    private lateinit var request: Request
    private val apiAddress = Api()
    private lateinit var context: Context
    private var cache = HashMap<String, String>()
    var translating = false

    fun init(context: Context) {
        this.context = context
    }

    fun isApiLive() {
        try {
            Log.d("checkAPILive", "none")
            request = Request.Builder()
                    .url(apiAddress.url_request("Hello"))
                    .build()

            val response = client.newCall(request).execute()

            Log.d("body", response.body()!!.toString())

            if (response.isSuccessful) {
                Log.d("checkAPILive", "true")
                onDetect.onAPILive()
            }
            else {
                Log.d("checkAPILive", "false")
                onDetect.onAPIError()
            }
        } catch (e: java.lang.Exception){
            Log.d("checkAPILive", "false")
            e.printStackTrace()
            onDetect.onAPIError()
        }
    }

    fun toVietnamese(input: String) {
        translating = true
        onDetect.onDetected(input)
        onDetect.onTranslated("(Đang dịch...)")
        if (cache.containsKey(input)) {
           onDetect.onTranslated(cache[input]!!)
        } else {
            try {
                var newInput = input
                while (newInput.indexOf("\n") != -1) {
                    newInput = newInput.replace("\n", "%20%0A")
                }

                request = Request.Builder()
                        .url(apiAddress.url_request(newInput))
                        .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful && response.body() != null ) {
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
                onDetect.onAPIError()
            }
        }
        translating = false
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