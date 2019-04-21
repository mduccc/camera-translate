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


class ModeActivity : AppCompatActivity(), ModeClickedEvent {
    override fun startCamera() {
        mode_real_time.setOnClickListener {
            if (hasPermission()) {
                val intent = Intent(this, Cam2RealTimeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_in_bottom)
            }
        }
    }

    override fun share() {
        share_app.setOnClickListener {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Máy ảnh dịch \n https://play.google.com/store/apps/details?id=com.indieteam.cameratranslate")
            startActivity(Intent.createChooser(sharingIntent, "Chia sẻ qua"))
        }
    }

    override fun appInfo() {
        app_info.setOnClickListener {
            val intent = Intent(this, AppInfoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.design_snackbar_in, R.anim.design_snackbar_in)
        }
    }

    private val requestPermission = 0
    private var mode = -1

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == requestPermission) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
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
    private fun setInfo() {
        val packageManager = packageManager.getPackageInfo(packageName, 0)
        version.text = "V ${packageManager.versionName}"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)
        setInfo()
        startCamera()
        share()
        appInfo()
    }
}
