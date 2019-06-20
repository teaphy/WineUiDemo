package com.teaphy.wineuidemo.scan

import android.content.Context
import android.graphics.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.view.WindowManager
import androidx.camera.core.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.activity_scan.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.*
import java.util.concurrent.Executors


@Route(path = "/account/scan")
class ScanActivity : AppCompatActivity() {

	private var lastAnalyzedTimestamp = 0L

	//		// 解析 二维码 Bitmap 的 reader
	val multiFormatReader: MultiFormatReader = MultiFormatReader()

	val scanHandler: Handler = object : Handler(Looper.getMainLooper()) {
		override fun handleMessage(msg: Message?) {

		}
	}

	var mExecutor = Executors.newFixedThreadPool(5)



	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scan)

		val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)

		hints[DecodeHintType.TRY_HARDER] = true
		hints[DecodeHintType.PURE_BARCODE] = true
		hints[DecodeHintType.POSSIBLE_FORMATS] = arrayListOf(BarcodeFormat.QR_CODE)
		multiFormatReader.setHints(hints)


		initCamera()
	}

	private fun initCamera() {

		// 旋转角度
		val rotationCamera = getRotation(CameraX.LensFacing.BACK)
		// 分辨率
		val resolutionCamera = Size(480, 640)

		// 1. 创建Preview用例
		// 为取景器(viewfinder)用例创建配置对象

		val previewConfig = PreviewConfig.Builder().apply {
			// 设置回调处理的Handler
//			setCallbackHandler(scanHandler)
			// 根据镜头所朝向的方向设置要配置的主摄像头
			setLensFacing(rotationCamera.first)
			// 设置指定宽高比
//			setTargetAspectRatio(Rational(1, 1))
			// 设置要配置的目标对象的名称。
			// Preview.class.getCanonicalName() + "-" + UUID.randomUUID()
			setTargetName("teaphy")
			// 指定分辨率
//			setTargetResolution(Size(640, 640))
			// setTargetRotation(int)：设置此配置中图像的预期目标的旋转。
//			setTargetResolution(resolutionCamera)
//			setTargetRotation(rotationCamera.second)
//			setTargetResolution(cameraResolution)
			setTargetResolution(Size(1080, 1920))
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
//			updateTransform()
		}

		// 2. 创建ImageAnalysis用例
		val imageAnalysisConfig = ImageAnalysisConfig.Builder().apply {
			// 设置指定宽高比
//			setTargetAspectRatio(Rational(1, 1))
//			setTargetResolution(resolutionCamera)
//			setTargetRotation(rotationCamera.second)
//			val analyzerThread = HandlerThread(
//				"LuminosityAnalysis"
//			).apply { start() }
//			setCallbackHandler(Handler(analyzerThread.looper))
			setImageQueueDepth(1)
			setTargetResolution(Size(1080, 1920))
			setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
			val analyzerThread = HandlerThread("BarcodeAnalyzer").apply { start() }
			setCallbackHandler(Handler(analyzerThread.looper))
		}.build()
		val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
		imageAnalysis.analyzer = ImageAnalysis.Analyzer { image, _ ->

			val buffer = image.planes[0].buffer
			val data = ByteArray(buffer.remaining())
			val height = image.height
			val width = image.width
			buffer.get(data)
			val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)

			val bitmap = BinaryBitmap(HybridBinarizer(source))

			try {
				val result = multiFormatReader.decode(bitmap)
				scanHandler.post {
					value_text.text = "value: ${result.text}"
				}
				Log.e("teaphy", "resolved!!! = $result")
			} catch (e: Exception) {
				Log.d("teaphy", "Error decoding barcode")
			}

		}


		// 将UserCase的集合绑定到LifecycleOwner
		CameraX.bindToLifecycle(this, preview, imageAnalysis)
	}


	private fun getRotation(lensFacing: CameraX.LensFacing = CameraX.LensFacing.BACK): Pair<CameraX.LensFacing, Int> {
		// 获取Activity的方向
		val rotation = windowManager.defaultDisplay.rotation

		val degrees = when (rotation) {
			Surface.ROTATION_0 -> 0
			Surface.ROTATION_90 -> 90
			Surface.ROTATION_180 -> 180
			Surface.ROTATION_270 -> 270
			else -> {
				if (rotation % 90 == 0) {
					(360 + rotation) % 360
				} else {
					throw IllegalArgumentException("Bad rotation: $rotation")
				}
			}
		}

		return if (lensFacing == CameraX.LensFacing.BACK) {
			Pair(lensFacing, degrees)
		} else {
			Pair(lensFacing, (360 - degrees) % 360)
		}
	}

	private fun updateTransform() {
		val matrix = Matrix()

		// 1. 计算ViewFinder的中心点
		val centerX = view_finder.width / 2f
		val centerY = view_finder.height / 2f

		if (view_finder.display == null) {
			return
		}

		// 2. 更正预览输出以考虑显示旋转
		val rotationDegrees = when (view_finder.display.rotation) {
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
