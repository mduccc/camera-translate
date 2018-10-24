package com.indieteam.cameratranslate.process

import android.content.Context
import android.media.Image
import android.net.Uri
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.indieteam.cameratranslate.ui.Cam2RealTimeActivity
import com.indieteam.cameratranslate.ui.ResultActivity
import kotlinx.android.synthetic.main.activity_cam2_real_time.*
import kotlinx.android.synthetic.main.activity_result.*

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
            runOnUiThread { text_detected.text = localResult }
            frame = 0
            cloudTranslate.toVietnamese(localResult)
        }
    }

    private fun updateOne(){
        (context as ResultActivity).apply {
            cloudTranslate.toVietnamese(localResult)
        }
    }

    fun run(image: Image?, rotation: Int?, uri: Uri?){
        localResult = ""
        val imageVision: FirebaseVisionImage
        if (mode == "RealTime") {
            (context as Cam2RealTimeActivity).frame = 1
            imageVision = FirebaseVisionImage.fromMediaImage(image!!, rotation!!)
        }else{ imageVision = FirebaseVisionImage.fromFilePath(context as ResultActivity, uri!!) }

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
                            if (mode == "RealTime") updateRealTime() else updateOne()
                        }else {
                            (context as Cam2RealTimeActivity).apply {
                                runOnUiThread {
                                    text_translated.text = ""
                                    text_detected.text = ""
                                    frame = 0
                                }
                            }
                        }
                    }

                    if (it.result == null) {
                        (context as Cam2RealTimeActivity).apply {
                            runOnUiThread {
                                frame = 0
                                text_translated.text = ""
                            }
                        }
                    }
                    image?.close()
                }
                .addOnFailureListener { _ ->
                    Log.d("Recognizer Listen", "False")
                    if (mode == "RealTime") (context as Cam2RealTimeActivity).frame = 0
                }
    }

}