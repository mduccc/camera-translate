package com.indieteam.cameratranslate.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.indieteam.cameratranslate.R
import kotlinx.android.synthetic.main.activity_mode.*

class ModeActivity : AppCompatActivity() {

    private val requestSelectPhoto = 1
    private val requestPermission = 0
    private var mode = -1
    val intentObj = Intent()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == requestPermission){
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED)
            {
                when(mode){
                    0 -> singlePhotoMode()
                    1 -> realTimeMode()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == requestSelectPhoto){
            val intent = Intent(this, ResultActivity::class.java)
                    .putExtra("uriPhoto", data.dataString)
            startActivity(intent)
        }
    }

    private val hasPermission = {
        if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), requestPermission)
            false
        } else {
            true
        }
    }

    private var singlePhotoMode = {

        intentObj.apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        ActivityCompat.startActivityForResult(
                this,
                Intent.createChooser(intentObj, "Select Picture"),
                requestSelectPhoto,
                null)
    }

    private var realTimeMode = {
        val intent = Intent(this, Cam2RealTimeActivity::class.java)
        startActivity(intent)
    }

    private val selectMode = {
        mode_photo.setOnClickListener {
            mode = 0
            if (hasPermission())
                singlePhotoMode()
        }
        mode_real_time.setOnClickListener {
            mode = 1
            if (hasPermission())
                realTimeMode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)
        selectMode()
    }
}
