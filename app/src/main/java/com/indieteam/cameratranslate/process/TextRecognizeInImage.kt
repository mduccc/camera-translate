package com.indieteam.cameratranslate.process

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.ResultActivity
import kotlinx.android.synthetic.main.activity_cam2_real_time.*

class TextRecognizeInImage(private val context: Context, private val mode: String) {

    private val textRecognizer = FirebaseVision.getInstance()
            .onDeviceTextRecognizer

    private var localResult = ""
    private val cloudTranslate = CloudTranslate()

    init {
        if (mode == "RealTime")
            cloudTranslate.init(context, "RealTime")
        else
            cloudTranslate.init(context, "one")
    }

    private fun updateRealTime(){
        (context as Cam2RealTimeActivity).apply {
            runOnUiThread {
                handler?.post {
                    cloudTranslate.toVietnamese(localResult)
                }
            }
        }
    }

    private fun updateOne(){
        (context as ResultActivity).apply {
            cloudTranslate.toVietnamese(localResult)
        }
    }

    fun build(){
        if (mode == "RealTime"){
            context as Cam2RealTimeActivity
            val bitmap = context.texture_preview.getBitmap(context.previewWidth/4, context.previewHeight/4)
            context.handler?.post {
                run(bitmap, null)
            }
        }
    }

    fun run(bitmap: Bitmap?, uri: Uri?){
        localResult = ""
        val imageVision: FirebaseVisionImage
        if (mode == "RealTime") {
            imageVision = FirebaseVisionImage.fromBitmap(bitmap!!)
        }else{
            imageVision = FirebaseVisionImage.fromFilePath(context as ResultActivity, uri!!)
        }

        textRecognizer.processImage(imageVision)
                .addOnCompleteListener { it ->
                    it.result?.let { it ->
                        for (block in it.textBlocks){
                            block.confidence?.let {
                                Log.d("confidence", it.toString())
                            }
                            for (line in block.lines){
                                for (element in line.elements){
                                    Log.d("element", element.text.toString())
                                    localResult += element.text + " "
                                }
                                localResult += "\n"
                            }
                        }
                        Log.d("Text", localResult)
                        if (localResult.isNotBlank()) {
                            localResult = localResult.trim()
                            if (mode == "RealTime")
                                updateRealTime()
                            else
                                updateOne()
                        }else {
                            (context as Cam2RealTimeActivity).apply {
                                runOnUiThread {
                                    text_detected.text = ""
                                    text_translated.text = ""
                                    textRecognizeInImage.build()
                                }
                            }
                        }
                    }

                    if (it.result == null) {
                        (context as Cam2RealTimeActivity).apply {
                            runOnUiThread {
                                text_detected.text = ""
                                text_translated.text = ""
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d("Recognizer Listen", "False")
                }
    }

}