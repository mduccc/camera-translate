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

    private val requestPermission = 0
    private var mode = -1

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == requestPermission){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                when(mode){
                    1 -> realTimeMode()
                }
            }
        }
    }

    private val hasPermission = {
        if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), requestPermission)
            false
        } else {
            true
        }
    }

    private var realTimeMode = {
        val intent = Intent(this, Cam2RealTimeActivity::class.java)
        startActivity(intent)
    }

    private val selectMode = {
        mode_real_time.setOnClickListener {
            mode = 1
            if (hasPermission())
                realTimeMode()
        }
    }

    private fun setInfo() {
        val packageManager = packageManager.getPackageInfo(packageName, 0)
        version.text = "V ${packageManager.versionName}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)
        setInfo()
        selectMode()
    }
}
