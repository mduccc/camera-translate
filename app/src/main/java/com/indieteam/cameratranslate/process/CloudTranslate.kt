package com.indieteam.cameratranslate.process

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.ResultActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class CloudTranslate{

    private val client = OkHttpClient()
    private val apiAddress = ApiAddress()
    private lateinit var context: Context
    private lateinit var mode: String

    fun init(context: Context, mode: String){
        this.context = context
        this.mode = mode
    }

    fun toVietnamese(text: String){
        object : Thread() {
            override fun run() {
                try {
                    val request = Request.Builder()
                            .url(apiAddress.url_request(text))
                            .build()
                    val response = client.newCall(request).execute()
                    if (response.body() != null) {
                        val bodyObj = JSONObject(response.body()!!.string())
                        Log.d("Body Result", bodyObj.toString())
                        if (bodyObj.getString("code") == "200") {
                            val resultArr = bodyObj.getJSONArray("text")
                            var result = ""
                            for (i in 0 until resultArr.length()) {
                                result += resultArr[i]
                            }
                            result = result.toLowerCase()
                            if (mode == "RealTime"){
                                if (result.isNotBlank()) {
                                    (context as Cam2RealTimeActivity).apply {
                                        runOnUiThread {
                                            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }else{
                                if (result.isNotBlank()) {
                                    (context as ResultActivity).apply {
                                        runOnUiThread {
                                            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            Log.d("Translate result", result)
                        }else Log.d("code", bodyObj.getString("code"))
                    }else Log.d("response", "null")

                }catch (e: Exception){ Log.d("Logic Err", e.toString())}
            }
        }.start()
    }

}