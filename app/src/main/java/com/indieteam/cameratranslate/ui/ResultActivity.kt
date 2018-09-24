package com.indieteam.cameratranslate.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.indieteam.cameratranslate.R
import com.indieteam.cameratranslate.process.TextRecognizeInImage
import kotlinx.android.synthetic.main.activity_result.*


class ResultActivity : AppCompatActivity() {

    private lateinit var uriPhoto: Uri
    private lateinit var textRecognizeInImage: TextRecognizeInImage

    private fun addPhoto(){
        Glide.with(this)
                .load(uriPhoto) // Uri of the picture
                .listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        textRecognizeInImage.run(null, null, uriPhoto)
                        return false
                    }
                })
                .into(input_image)
    }

    private fun init(){
        uriPhoto = Uri.parse(intent.getStringExtra("uriPhoto"))
        textRecognizeInImage = TextRecognizeInImage(this, "one")
    }

    private fun run(){ init(); addPhoto() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        run()
    }
}
