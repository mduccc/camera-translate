package com.indieteam.cameratranslate.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class DrawArea(context: Context): View(context){

    private val paint = Paint()
    private var widthPercent = 0f
    private var heightPercent = 0f

    private fun setScreenPercent(){
        (context as Cam2RealTimeActivity).apply {
            widthPercent = sWidth.toFloat()/100f
            heightPercent = sHeight.toFloat()/100f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setScreenPercent()
        canvas?.let { drawArea(it) }
    }

    private fun drawArea(canvas: Canvas){
        paint.apply { strokeWidth = 5f; color = Color.WHITE; style = Paint.Style.STROKE }
        canvas.drawRect(widthPercent*5, heightPercent*30, widthPercent*95, heightPercent*50, paint)
    }

}