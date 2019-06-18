package com.teaphy.wineuidemo.camera

import android.Manifest
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.activity_take_photo.*
import permissions.dispatcher.*
import java.io.File

@Route(path = "/account/takePhoto")
@RuntimePermissions
class TakePhotoActivity : AppCompatActivity() {

	val previewHandler: Handler = object :Handler(Looper.getMainLooper()) {
		override fun handleMessage(msg: Message?) {
			Log.e("teaphy", "msg: $msg")
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_take_photo)

		start_camera_button.setOnClickListener {
			showCamera()
		}
	}

	@NeedsPermission(Manifest.permission.CAMERA)
	fun showCamera() {
		view_finder.post { startCamera() }
	}


	private fun startCamera() {

		// 1. 创建Preview用例
		// 为取景器(viewfinder)用例创建配置对象
		val previewConfig = PreviewConfig.Builder().apply {
			// 设置回调处理的Handler
			setCallbackHandler(previewHandler)
			// 根据镜头所朝向的方向设置要配置的主摄像头
			setLensFacing(CameraX.LensFacing.BACK)
			// 设置指定宽高比
			setTargetAspectRatio(Rational(1, 1))
			// 设置要配置的目标对象的名称。
			// Preview.class.getCanonicalName() + "-" + UUID.randomUUID()
			setTargetName("teaphy")
			// 指定分辨率
			setTargetResolution(Size(640, 640))
			// setTargetRotation(int)：设置此配置中图像的预期目标的旋转。
		}.build()

		// 创建取景器(viewfinder)用例
		val preview = Preview(previewConfig)

		// 设置取景器更新监听
		// 每次更新取景器时，都会重新计算布局
		preview.setOnPreviewOutputUpdateListener {

			val parent = view_finder.parent as ViewGroup
			parent.removeView(view_finder)
			parent.addView(view_finder, 0)

			view_finder.surfaceTexture = it.surfaceTexture
			updateTransform()
		}

		// 创建ImageCapture配置实例
		val imageCaptureConfig = ImageCaptureConfig.Builder()
			.apply {
				// 设置图像宽高比
				setTargetAspectRatio(Rational(1, 1))
				// 设置ImageCapture.CaptureMode
				setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
			}.build()

		// 创建ImageCapture实例
		val imageCapture = ImageCapture(imageCaptureConfig)
		findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
			val file = File(externalMediaDirs.first(),
				"${System.currentTimeMillis()}.jpg")
			imageCapture.takePicture(file,
				object : ImageCapture.OnImageSavedListener {
					override fun onError(error: ImageCapture.UseCaseError,
					                     message: String, exc: Throwable?) {
						val msg = "Photo capture failed: $message"
						Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
						Log.e("CameraXApp", msg)
						exc?.printStackTrace()
					}

					override fun onImageSaved(file: File) {
						Glide.with(this@TakePhotoActivity)
							.load(file)
							.into(image_view)
					}
				})
		}

		// 将UserCase的集合绑定到LifecycleOwner
		CameraX.bindToLifecycle(this, preview, imageCapture)
	}

	private fun updateTransform() {
		val matrix = Matrix()

		// 1. 计算ViewFinder的中心点
		val centerX = view_finder.width / 2f
		val centerY = view_finder.height / 2f

		// 2. 更正预览输出以考虑显示旋转
		val rotationDegrees = when(view_finder.display.rotation) {
			Surface.ROTATION_0 -> 0
			Surface.ROTATION_90 -> 90
			Surface.ROTATION_180 -> 180
			Surface.ROTATION_270 -> 270
			else -> return
		}
		matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

		// 3. 将转换应用于TextureView
		view_finder.setTransform(matrix)
	}
}
