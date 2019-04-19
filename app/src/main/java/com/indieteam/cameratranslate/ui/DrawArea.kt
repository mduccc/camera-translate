package com.indieteam.cameratranslate.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_cam2_real_time.*

class DrawArea(context: Context): View(context){

    private val paint = Paint()
    private var widthPercent = 0f
    private var heightPercent = 0f
    private val posArr = IntArray(2)

    private fun setScreenPercent(){
        (context as Cam2RealTimeActivity).apply {
            widthPercent = previewWidth.toFloat()/100f
            heightPercent = previewHeight/100f
            texture_preview.getLocationOnScreen(posArr)
        }
    }

    private fun drawArea(canvas: Canvas){
        Log.d("texture_preview pos", "${posArr[0]}, ${posArr[1]}")
        paint.apply { strokeWidth = 5f; color = Color.WHITE; style = Paint.Style.STROKE }
        canvas.drawRect(widthPercent*5, posArr[1].toFloat() , widthPercent*95, posArr[1].toFloat() + (context as Cam2RealTimeActivity).texture_preview.height, paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setScreenPercent()
        canvas?.let { drawArea(it) }
    }

}