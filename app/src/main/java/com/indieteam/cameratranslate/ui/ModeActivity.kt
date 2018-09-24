package com.indieteam.cameratranslate.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.indieteam.cameratranslate.R
import kotlinx.android.synthetic.main.activity_mode.*

class ModeActivity : AppCompatActivity() {

    private val requestCode = 1
    private val intentObj = Intent()
    private var permissions = 0

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == this.requestCode){
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                this.permissions = 1
                run()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == requestCode){
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("uriPhoto", data.dataString)
            startActivity(intent)
        }else{ Toast.makeText(this, "receive image data false", Toast.LENGTH_SHORT).show() }
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
        }else permissions = 1
    }

    private fun selectMode(){
        mode_photo.setOnClickListener {
            intentObj.apply { type = "image/*"; action = Intent.ACTION_GET_CONTENT }
            ActivityCompat.startActivityForResult(this, Intent.createChooser(intentObj, "Select Picture"), requestCode, null)
        }
        mode_real_time.setOnClickListener {
            val intent = Intent(this, Cam2RealTimeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun run(){ selectMode() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)
        checkPermission()
        if (permissions == 1) run()
    }
}
