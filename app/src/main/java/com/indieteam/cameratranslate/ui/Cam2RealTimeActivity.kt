package com.indieteam.cameratranslate.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View.GONE
import android.view.View.VISIBLE
import com.indieteam.cameratranslate.R
import com.indieteam.cameratranslate.process.CloudTranslate
import com.indieteam.cameratranslate.process.TextRecognizeInImage
import kotlinx.android.synthetic.main.activity_cam2_real_time.*
import android.app.ProgressDialog
import android.app.Dialog
import android.widget.Toast


@Suppress("DEPRECATION")
class Cam2RealTimeActivity : AppCompatActivity() {

    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCharacteristics: CameraCharacteristics? = null
    private var captureRequest: CaptureRequest.Builder? = null
    var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    lateinit var textRecognizeInImage: TextRecognizeInImage
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLook: PowerManager.WakeLock
    private lateinit var drawArea: DrawArea
    var imageShowed = false
    var click = false

    var sWidth = 0
    var sHeight = 0
    var previewWidth = 0
    var previewHeight = 0
    private var camWidth = 0
    private var camHeight = 0
    private var camBack = "null"
    private var camFront = "null"
    private var resume = 0

    private val posArr = IntArray(2)
    private lateinit var dialog: Dialog

    private val onDetect = object : OnDetect {
        override fun onAPIError() {
            runOnUiThread {
                //dialog.cancel()
                Toast.makeText(this@Cam2RealTimeActivity, "Kiểm tra kết nối mạng", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        override fun onAPILive() {
            runOnUiThread {
                //dialog.cancel()
            }
        }

        override fun onDetected(text: String) {
            runOnUiThread {
                text_detected.text = text
            }
        }

        override fun onTranslated(text: String) {
            runOnUiThread {
                text_translated.text = text
            }
        }
    }

    private fun checkAPILive() {
        handler?.post {
            CloudTranslate(onDetect).isApiLive()
        }
    }


    private var hasCamera = { packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) }

    private fun getCameraId() {
        try {
            for (i in cameraManager!!.cameraIdList) {
                Log.d("Camera Id", i)
                if (cameraManager!!.getCameraCharacteristics(i).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    if (camBack == "null")
                        camBack = i
                }

                if (cameraManager!!.getCameraCharacteristics(i).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    camFront = i
            }
        } catch (e: Exception) {
        }
    }

    inner class CameraOpenCallBack : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            this@Cam2RealTimeActivity.cameraDevice = camera
            getCameraSize()
            CameraPreview().startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            this@Cam2RealTimeActivity.cameraDevice!!.close()
            Log.d("CameraOpenCallBack", "onDisconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            this@Cam2RealTimeActivity.cameraDevice!!.close()
            Log.d("CameraOpenCallBack", "onDisconnected")
        }

    }

    inner class CaptureCallback : CameraCaptureSession.CaptureCallback() {
        init {
            imageShowed = true
            Log.d("Image Show", "Showed")
        }
    }

    inner class CaptureSessionCallBack : CameraCaptureSession.StateCallback() {

        override fun onConfigured(session: CameraCaptureSession) {
            try {
                session.setRepeatingRequest(captureRequest!!.build(), CaptureCallback(), handler)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d("CaptureSessionCallBack", "onConfigureFailed")
        }

    }

    inner class CameraPreview {

        fun startPreview() {
            setPreviewSize()
            val texture_view_params = texture_preview.layoutParams as ConstraintLayout.LayoutParams
            texture_view_params.apply {
                width = previewWidth
                height = previewHeight
            }
            texture_preview.layoutParams = texture_view_params

            val detected_layout_params = detected_layout.layoutParams as ConstraintLayout.LayoutParams
            detected_layout_params.apply {
                width = previewWidth
                height = previewHeight
            }
            detected_layout.layoutParams = detected_layout_params

            drawArea = DrawArea(this@Cam2RealTimeActivity)
            cam_realtime_layout.addView(drawArea)
            val surfaceTexture = texture_preview.surfaceTexture

            scroll_view_detected.y = posArr[1].toFloat() + 30f
            scroll_view_translated.y = posArr[1].toFloat() + texture_preview.height - 30f - scroll_view_translated.height

            //Do phan giai nay se hien thi o tren man hinh preview
            surfaceTexture.setDefaultBufferSize(previewWidth, previewHeight)

            val outputSurface = arrayListOf<Surface>(Surface(surfaceTexture))

            try {
                captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest!!.apply {
                    addTarget(Surface(surfaceTexture))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                cameraDevice!!.createCaptureSession(outputSurface, CaptureSessionCallBack(), null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    internal inner class SurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun textureViewListen() {
        texture_preview!!.surfaceTextureListener = SurfaceTextureListener()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1)
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                run()
            }
    }

    private fun getCameraSize() {
        try {
            cameraCharacteristics = cameraManager!!.getCameraCharacteristics(camBack)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cameraCharacteristics?.let {
            val configMap = it.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val size = configMap!!.getOutputSizes(ImageFormat.YUV_420_888)
            // Sort max first
            for (i in 0 until size.size - 1)
                for (j in i + 1 until size.size)
                    if (size[i].width < size[j].width) {
                        val temp = size[i]
                        size[i] = size[j]
                        size[j] = temp
                    }

            val needRatio = 4f / 3f
            Log.d("Need Radio", needRatio.toString())
            for (i in size) {
                Log.d("Camera Size", "Width: ${i.width}, Height: ${i.height}")
                val ratio = i.width.toFloat() / i.height.toFloat()
                Log.d("Real Ratio", ratio.toString())
                if (ratio == needRatio /*&& i.width.toFloat() <= sWidth*/) {
                    Log.d("Camera Size Selected", "Width: ${i.width}, Height: ${i.height}")
                    camWidth = i.height
                    camHeight = i.width
                    break
                }
            }
        }
    }

    private fun setPreviewSize() {
        previewWidth = sWidth
        previewHeight = (sHeight / 4) * 3
    }

    private fun setScreenSize() {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        sWidth = point.x
        sHeight = point.y
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            cameraManager!!.openCamera(camBack, CameraOpenCallBack(), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        ORIENTATIONS.let {
            it.append(Surface.ROTATION_0, 90)
            it.append(Surface.ROTATION_90, 0)
            it.append(Surface.ROTATION_180, 270)
            it.append(Surface.ROTATION_270, 180)
        }

        cameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        setScreenSize()
        getCameraId()

        Log.d("Camera Back", camBack)
        if (camBack == "null")
            finish()
        Log.d("Preview Width", previewWidth.toString())
        Log.d("Preview Height", previewHeight.toString())

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        textRecognizeInImage = TextRecognizeInImage(this, onDetect)
    }

    private fun startHandlerThread() {
        handlerThread = HandlerThread("Thread_camera2")
        handlerThread!!.start()
        handler = Handler(handlerThread!!.looper)
    }

    private fun cancelHandlerThread() {
        if (handlerThread != null && handler != null) {
            try {
                handler!!.removeCallbacks(handlerThread)
                handlerThread!!.quitSafely()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun run() {
        getPosOfTexture()
        textureViewListen()
    }

    companion object {
        val ORIENTATIONS = SparseIntArray()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        action?.let {
            if (action == MotionEvent.ACTION_DOWN) {
                Log.d("Touch", "DOWN")
                click = true
                title_real_time.visibility = VISIBLE
                if (!textRecognizeInImage.cloudTranslate.translating)
                    textRecognizeInImage.catchImage()
            }
            if (action == MotionEvent.ACTION_UP) {
                Log.d("Touch", "UP")
                click = false
                title_real_time.visibility = GONE
            }
        }

        return super.onTouchEvent(event)
    }

    private fun getPosOfTexture(){
        texture_preview.getLocationOnScreen(posArr)
    }

    private fun showDialog() {
        dialog = ProgressDialog.show(this, "Đang kết nối tới máy chủ",
                "Vui lòng đợi...", true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    @SuppressLint("InvalidWakeLockTag")
    override fun onResume() {
        super.onResume()
        wakeLook = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeLook")
        wakeLook.acquire(10 * 60 * 1000L /*10 minutes*/)
        startHandlerThread()

        if (resume == 0) {
            //showDialog()
            //checkAPILive()
            if (hasCamera())
                run()
        }

        resume++
        if (resume > 1)
            openCamera()
    }

    override fun onPause() {
        super.onPause()
        try {
            cameraDevice!!.close()
            cancelHandlerThread()
            wakeLook.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cam2_real_time)
        init()
    }
}
