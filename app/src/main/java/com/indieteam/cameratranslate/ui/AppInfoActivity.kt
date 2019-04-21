package com.indieteam.cameratranslate.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.indieteam.cameratranslate.R
import kotlinx.android.synthetic.main.activity_app_info.*

class AppInfoActivity : AppCompatActivity() {

    private fun setInfo() {
        val packageManager = packageManager.getPackageInfo(packageName, 0)
        app_version.text = "Version ${packageManager.versionName}"
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)
        setInfo()
    }
}
