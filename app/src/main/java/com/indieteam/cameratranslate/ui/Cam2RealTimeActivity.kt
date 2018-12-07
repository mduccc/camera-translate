package com.indieteam.cameratranslate.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.RelativeLayout
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.indieteam.cameratranslate.R
import com.indieteam.cameratranslate.process.TextRecognizeInImage
import kotlinx.android.synthetic.main.activity_cam2_real_time.*

import java.util.ArrayList

class Cam2RealTimeActivity : AppCompatActivity() {

    private val codeRequest = 1
    private var permissions = 0
    private var camManager: CameraManager? = null
    private var camera2: CameraDevice? = null
    var sWidth = 0
    var sHeight = 0
    private var previewWidth = 0
    var previewHeight = 0
    private var camWidth = 0
    private var camHeight = 0
    private var cameraCharacteristics: CameraCharacteristics? = null
    private var imageReader: ImageReader? = null
    private val quality = 3
    private var cameraRequest: CaptureRequest.Builder? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private lateinit var textRecognizeInImage: TextRecognizeInImage
    private lateinit var power: PowerManager
    private lateinit var wakeScreen: PowerManager.WakeLock
    var frame = 0
    private lateinit var drawArea: DrawArea

    private fun cameraId(): ArrayList<String> {
        val list = ArrayList<String>()
        try { for (i in camManager!!.cameraIdList) { list.add(i) }
        } catch (e: Exception) { Log.d("Logic Err", e.toString()) }
        return list
    }

    private var countResume = 0

    inner class CameraOpenCallBack : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            this@Cam2RealTimeActivity.camera2 = camera
            getCameraSize()
            CameraPreview().startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            this@Cam2RealTimeActivity.camera2!!.close()
            Log.d("Err Logic", "onDisconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            this@Cam2RealTimeActivity.camera2!!.close()
            Log.d("Err Logic", "onDisconnected")
        }

    }

    inner class CaptureSessionCallBack : CameraCaptureSession.StateCallback() {

        override fun onConfigured(session: CameraCaptureSession) {
            try { session.setRepeatingRequest(cameraRequest!!.build(), null, backgroundHandler)
            } catch (e: Exception) { Log.d("Logic Err", e.toString()) }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d("Logic Err", "onConfigureFailed")
        }

    }

    inner class OnFrame: ImageReader.OnImageAvailableListener{
        override fun onImageAvailable(p0: ImageReader?) {
            val image = p0?.acquireNextImage()
            //Log.d("Frame", frame.toString())
            if (image != null && frame == 0){
                textRecognizeInImage.run(image, getRotationCamera(), null)
            }
            image?.close()
        }

    }

    inner class CameraPreview {

        fun startPreview() {
            val texture_view_params = texture_preview.layoutParams as ConstraintLayout.LayoutParams
            texture_view_params.apply { width = previewWidth; height = previewHeight }
            texture_preview.layoutParams = texture_view_params

            val detected_layout_params = detected_layout.layoutParams as ConstraintLayout.LayoutParams
            detected_layout_params.apply { width = previewWidth; height = previewHeight }
            detected_layout.layoutParams = detected_layout_params

            drawArea = DrawArea(this@Cam2RealTimeActivity)
            cam_realtime_layout.addView(drawArea)
            val surfaceTexture = texture_preview.surfaceTexture
            //Do phan giai nay se hien thi o tren man hinh preview
            surfaceTexture.setDefaultBufferSize(previewWidth, previewHeight)
            val outputSurface = arrayListOf<Surface>(Surface(surfaceTexture), imageReader!!.surface)

            try { cameraRequest = camera2!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            } catch (e: Exception) { Log.d("Logic Err", e.toString()) }

            cameraRequest?.let {
                it.apply { addTarget(Surface(surfaceTexture)); addTarget(imageReader!!.surface) }
                try { camera2!!.createCaptureSession(outputSurface, CaptureSessionCallBack(), null)
                } catch (e: Exception) { Log.d("Logic Err", e.toString()) }
            }
        }

    }

    internal inner class SurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) { openCamera2() }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean { return false }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun textureViewListen() { texture_preview!!.surfaceTextureListener = SurfaceTextureListener() }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1)
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) { run() }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), codeRequest)
        else
            permissions = 1
    }

    private fun getCameraSize() {
        try { cameraCharacteristics = camManager!!.getCameraCharacteristics(cameraId()[0])
        } catch (e: Exception) { Log.d("Logic Err", e.toString()) }

        //sap xep giam dan
        cameraCharacteristics?.let{
            val configMap = it.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val size = configMap!!.getOutputSizes(ImageFormat.YUV_420_888)
            for (i in 0 until size.size - 1)
                for (j in i + 1 until size.size)
                    if (size[i].width < size[j].width) {
                        val temp = size[i]
                        size[i] = size[j]
                        size[j] = temp
                    }

            val needRatio = 4f / 3f
            Log.d("needRadio", needRatio.toString())
            for (i in size) {
                val ratio = i.width.toFloat() / i.height.toFloat()
                Log.d("realRatio", ratio.toString())
                if (ratio == needRatio) {
                    Log.d("cameraSize", "width: ${i.width}, height: ${i.height}")
                    camWidth = i.height
                    camHeight = i.width
                    break
                }
            }
        }
    }

    private fun setPreviewSize(){
        previewWidth = sWidth
        previewHeight = sWidth/3 * 4
    }

    private fun getScreenSize() {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        sWidth = point.x
        sHeight = point.y
    }

    private fun openCamera2() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        } else {
            try { camManager!!.openCamera(cameraId()[0], CameraOpenCallBack(), null)
            } catch (e: Exception) { Log.d("Logic Err", e.toString()) }
        }
    }

    private fun init() {
        ORIENTATIONS.let {
            it.append(Surface.ROTATION_0, 90)
            it.append(Surface.ROTATION_90, 0)
            it.append(Surface.ROTATION_180, 270)
            it.append(Surface.ROTATION_270, 180)
        }

        getScreenSize()
        setPreviewSize()
        Log.d("previewWidth", previewWidth.toString())
        Log.d("previewHeight", previewHeight.toString())
        textRecognizeInImage = TextRecognizeInImage(this, "RealTime")
        camManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        imageReader = ImageReader.newInstance(previewWidth / quality, previewHeight / quality, ImageFormat.YUV_420_888, 1)
        imageReader!!.setOnImageAvailableListener(OnFrame(), backgroundHandler)
        power = getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private fun handlerStart() {
        backgroundThread = HandlerThread("Thread_camera2")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun handlerCancel() {
        if (backgroundThread != null && backgroundHandler != null) {
            try {
                backgroundHandler!!.removeCallbacks(backgroundThread)
                backgroundThread!!.quitSafely()
            } catch (e: Exception) { Log.d("Logic Err", e.toString()) }
        }
    }

    private fun run() {
        textureViewListen()
    }

    companion object {
        val ORIENTATIONS = SparseIntArray()
    }

    // In document Google MLkit
    private fun getRotationCamera(): Int{
        val deviceRotation = windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        val sensorOrientation = camManager!!.getCameraCharacteristics(cameraId()[0])
                .get(CameraCharacteristics.SENSOR_ORIENTATION)
        rotationCompensation = (rotationCompensation + sensorOrientation!! + 270) / 360
        return when(rotationCompensation){
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> FirebaseVisionImageMetadata.ROTATION_90
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onResume() {
        super.onResume()
        wakeScreen = power.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeScreen")
        wakeScreen.acquire(10*60*1000L /*10 minutes*/)
        handlerStart()
        frame = 0
        countResume++
        if (countResume > 1) { handlerStart(); openCamera2() }
    }

    override fun onPause() {
        super.onPause()
        wakeScreen.release()
        try { camera2!!.close(); handlerCancel()
        } catch (e: Exception) { Log.d("Logic Err", e.toString()) }
    }

    override fun onBackPressed() {
        val intent = Intent(this, ModeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cam2_real_time)
        init()
        checkPermission()
        if (permissions == 1)
            run()
        else
            checkPermission()
    }
}
