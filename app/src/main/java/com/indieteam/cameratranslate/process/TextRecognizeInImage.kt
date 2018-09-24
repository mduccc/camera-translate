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

    private var resultForText = ""
    private var resultForTranslate = ""
    private var localResult = ""
    val cloudTranslate = CloudTranslate()

    init {
        if (mode == "RealTime")
            cloudTranslate.init(context, "RealTime")
        else
            cloudTranslate.init(context, "one")
    }
/*
    private var imgWidth = 0
    private var imgHeight = 0
*/

    private fun updateRealTime(){
        (context as Cam2RealTimeActivity).frame = 1
        context.apply {
            runOnUiThread { result.text = this@TextRecognizeInImage.resultForText }
            frame = 0
            cloudTranslate.toVietnamese(this@TextRecognizeInImage.resultForTranslate)
        }
    }

    private fun updateOne(){
        (context as ResultActivity).apply {
            runOnUiThread { result_.text = this@TextRecognizeInImage.resultForText }
            cloudTranslate.toVietnamese(this@TextRecognizeInImage.resultForTranslate)
        }
    }

    fun run(image: Image?, rotation: Int?, uri: Uri?){
       /* imgWidth = image!!.width
        imgHeight = image.height*/
        localResult = ""
        val imageVision: FirebaseVisionImage
        if (mode == "RealTime") {
            (context as Cam2RealTimeActivity).frame = 1
            imageVision = FirebaseVisionImage.fromMediaImage(image!!, rotation!!)
        }else{ imageVision = FirebaseVisionImage.fromFilePath(context as ResultActivity, uri!!) }
        textRecognizer.processImage(imageVision)
                .addOnCompleteListener { it ->
                    Log.d("Recognizer Listen", "True")

//                    for (block in it.resultForText.textBlocks){
//                        block.boundingBox?.let {
//                            if (it.top > imgHeight/100 *30 && it.bottom < imgHeight/100 * 50)
//                                localResult += block.text + "\n"
//                        }
//                    }'
                    localResult += it.result.text + "\n"
                    Log.d("Text", localResult)
                    if (localResult != resultForText){ resultForText = localResult }
                    localResult = ""
                    localResult += it.result.text + " "
                    Log.d("Text", localResult)
                    if (localResult != resultForTranslate){ resultForTranslate = localResult }
                    if (mode == "RealTime") updateRealTime() else updateOne()
                    image?.close()
                }
                .addOnFailureListener { _ ->
                    Log.d("Recognizer Listen", "False")
                    if (mode == "RealTime") (context as Cam2RealTimeActivity).frame = 0
                }
    }

}