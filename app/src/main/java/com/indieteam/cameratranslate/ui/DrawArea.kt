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
            widthPercent = sWidth.toFloat()/100f
            heightPercent = previewHeight/100f
            detected_layout.getLocationOnScreen(posArr)
        }
    }

    private fun drawArea(canvas: Canvas){
        paint.apply { strokeWidth = 5f; color = Color.WHITE; style = Paint.Style.STROKE }
        canvas.drawRect(widthPercent*5, heightPercent*5 + posArr[1].toFloat() , widthPercent*95, heightPercent*95 + posArr[1].toFloat(), paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setScreenPercent()
        canvas?.let { drawArea(it) }
    }

}