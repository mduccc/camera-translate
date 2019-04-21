package com.indieteam.cameratranslate.process

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.OnDetect
import kotlinx.android.synthetic.main.activity_cam2_real_time.*

class TextRecognizeInImage(private val context: Context, private val onDetect: OnDetect) {

    private val textRecognizer = FirebaseVision.getInstance()
            .onDeviceTextRecognizer

    val cloudTranslate = CloudTranslate(onDetect)

    init {
        cloudTranslate.init(context)
    }

    private fun updateRealTime(result: String) {
        (context as Cam2RealTimeActivity).apply {
            runOnUiThread {
                handler?.post {
                    cloudTranslate.toVietnamese(result)
                }
            }
        }
    }

    fun catchImage() {
        context as Cam2RealTimeActivity
        if (context.click) {
           onDetect.onEmpty()
            val bitmap = context.texture_preview.getBitmap(context.previewWidth / 4, context.previewHeight / 4)
            context.handler?.post {
                run(bitmap)
            }
        }
    }

    fun run(bitmap: Bitmap?) {
        var result = ""
        val imageVision: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap!!)

        textRecognizer.processImage(imageVision)
                .addOnCompleteListener { it ->
                    it.result?.let { it ->
                        for (block in it.textBlocks) {
                            block.confidence?.let {
                                Log.d("confidence", it.toString())
                            }
                            for (line in block.lines) {
                                for (element in line.elements) {
                                    Log.d("element", element.text.toString())
                                    result += element.text + " "
                                }
                                result += "\n"
                            }
                        }
                        Log.d("Text", result)

                        if (result.isNotBlank()) {
                            result = result.trim()
                            updateRealTime(result)
                        }
                    } ?: kotlin.run {
                        onDetect.onEmpty()
                        catchImage()
                    }
                }
                .addOnFailureListener {
                    Log.d("Recognizer Listen", "False")
                }
    }

}