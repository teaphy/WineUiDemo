package com.teaphy.wineuidemo.scan

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Telephony.Mms.Addr.CHARSET
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.activity_scan.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit

@Route(path = "/account/scan")
class ScanActivity : AppCompatActivity() {

	val previewHandler: Handler = object : Handler(Looper.getMainLooper()) {
		override fun handleMessage(msg: Message?) {
			Log.e("teaphy", "msg: $msg")
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scan)

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
			setCallbackHandler(previewHandler)
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
			setTargetResolution(resolutionCamera)
			setTargetRotation(rotationCamera.second)
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

		preview.focus(focus_vfv.frameFocus, focus_vfv.frameFocus, object : OnFocusListener{
			override fun onFocusUnableToLock(afRect: Rect?) {

			}

			override fun onFocusTimedOut(afRect: Rect?) {
			}

			override fun onFocusLocked(afRect: Rect?) {
			}

		})


		// 2. 创建ImageAnalysis用例
		val imageAnalysisConfig = ImageAnalysisConfig.Builder().apply {
			// 设置指定宽高比
//			setTargetAspectRatio(Rational(1, 1))
			setTargetResolution(resolutionCamera)
			setTargetRotation(rotationCamera.second)
		}.build()
		val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
		imageAnalysis.analyzer = ImageAnalysis.Analyzer { image, _ ->

			Thread {
				// Since format in ImageAnalysis is YUV, image.planes[0]
				// contains the Y (luminance) plane

				val buffer = image.planes[1].buffer
				// Extract image data from callback object
				val data = buffer.toByteArray()
				image.planes.forEach {
					Log.e("teaphy", "getPixelStride(): ${it.pixelStride}, getRowStride(): ${it.rowStride}")
				}
//				val yuvImage = YuvImage(data, ImageFormat.NV21, image.width, image.height, null)
//				val byteOutStream = ByteArrayOutputStream()
//
//				yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, byteOutStream)
//				val bmp = BitmapFactory.decodeByteArray(byteOutStream.toByteArray(),0, byteOutStream.size())
//
//				Glide.with(this@ScanActivity)
//					.load(bmp)
//					.into(image_view)

				val width = image.width
				val height = image.height
				val widthScreen = ScreenUtils.getScreenWidth()
				val heightScreen = ScreenUtils.getScreenHeight()

				Log.e("teaphy", "image - width: $width, height: $height, size: ${data.size}")
//				Log.e("teaphy", "bmp - bmp.size:${bitmap2Bytes(bmp).size} $, width: ${bmp.width}, height: ${bmp.height}")
//
				val focusRect = focus_vfv.frameFocus
				val captureRect = Rect(
					focusRect!!.left * 5 * resolutionCamera.height / ScreenUtils.getScreenWidth() / 8,
					focusRect.top * 5 * resolutionCamera.width / ScreenUtils.getScreenHeight() / 8,
					focusRect.right * 5 * resolutionCamera.height / ScreenUtils.getScreenWidth() / 8,
					focusRect.bottom * 5 * resolutionCamera.width / ScreenUtils.getScreenHeight() / 8
				)
				Log.e("teaphy", "captureRect: $captureRect")
//
				val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)


				// 可以解析的编码类型
				var decodeFormats = Vector<BarcodeFormat>()
				if (decodeFormats.isEmpty()) {
					decodeFormats = Vector()

					// 这里设置可扫描的类型，我这里选择了都支持
					decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
					decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
					decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS)
					decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS)
					decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS)
					decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS)
				}
				hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
				hints[DecodeHintType.CHARACTER_SET] = CHARSET
				hints[DecodeHintType.TRY_HARDER] = true
				hints[DecodeHintType.PURE_BARCODE] = true
				decode(data, image.height, image.width, captureRect, hints)
			}.run()


		}

		// 将UserCase的集合绑定到LifecycleOwner
		CameraX.bindToLifecycle(this, preview, imageAnalysis)
	}

//	public Bitmap Bytes2Bimap(byte[] b) {
//		if (b.length != 0) {
//			return BitmapFactory.decodeByteArray(b, 0, b.length);
//		} else {
//			return null;
//		}
//	}

//	public byte[] Bitmap2Bytes(Bitmap bm) {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//		return baos.toByteArray();
//	}
//

	fun bitmap2Bytes(bitmap: Bitmap): ByteArray {
		val baos = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
		return baos.toByteArray()
	}

	public fun bytes2Bimap(data: ByteArray): Bitmap? {
		return if (data.isNotEmpty()) {
			BitmapFactory.decodeByteArray(data, 0, data.size)
		} else {
			null
		}
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


	private fun decode(data: ByteArray, width: Int, height: Int, rect: Rect, hints: Map<DecodeHintType, Any>) {

//		// 解析 二维码 Bitmap 的 reader
		val multiFormatReader: MultiFormatReader = MultiFormatReader()

		multiFormatReader.setHints(hints)

		var rotatedData = ByteArray(data.size)

		if (width > height) { // portrait

			for (x in 0 until width) {
				for (y in 0 until height) {
					rotatedData[y * width + width - x - 1] = data[y + x * height]
				}
			}
		} else {
			rotatedData = data
		}

		var rawResult: Result? = null
		val source = buildLuminanceSource(rotatedData, width, height, rect)
		if (source != null) {
			val bitmap = BinaryBitmap(HybridBinarizer(source))
			try {
				rawResult = multiFormatReader.decodeWithState(bitmap)
			} catch (re: ReaderException) {
				Log.e("teaphy", "re: $re")
				// continue
			} finally {
				multiFormatReader.reset()
			}
		}

		if (rawResult != null) {
			Log.e("teaphysuc", "解析成功")
			Log.e("teaphysuc", "rawResult：$rawResult")
			ToastUtils.showShort("解析成功")
		} else {
			Log.e("teaphyfailure", "解析失败")
		}
	}


	fun buildLuminanceSource(data: ByteArray, width: Int, height: Int, rect: Rect): PlanarYUVLuminanceSource? {
//		val rect = focus_vfv.frameFocus ?: return null

// Go ahead and assume it's YUV rather than die.
		return PlanarYUVLuminanceSource(
			data, width, height, rect.left, rect.top,
			rect.right, rect.bottom, false
		)

//		val width_: Int
//		val height_: Int
//		if (width < height) {
//			width_ = width
//			height_ = height
//		} else {
//			width_ = height
//			height_ = width
//		}
//
//		return PlanarYUVLuminanceSource(
//			data, width, height, 0, 0,
//			width_, height_, false
//		)
	}

	private fun ByteBuffer.toByteArray(): ByteArray {
		rewind()    // Rewind the buffer to zero
		val data = ByteArray(remaining())
		get(data)   // Copy the buffer into a byte array
		return data // Return the byte array
	}

	private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
		private var lastAnalyzedTimestamp = 0L

		/**
		 * Helper extension function used to extract a byte array from an
		 * image plane buffer
		 */
		private fun ByteBuffer.toByteArray(): ByteArray {
			rewind()    // Rewind the buffer to zero
			val data = ByteArray(remaining())
			get(data)   // Copy the buffer into a byte array
			return data // Return the byte array
		}

		override fun analyze(image: ImageProxy, rotationDegrees: Int) {
			val currentTimestamp = System.currentTimeMillis()
			// Calculate the average luma no more often than every second
			if (currentTimestamp - lastAnalyzedTimestamp >=
				TimeUnit.SECONDS.toMillis(1)
			) {
				// Since format in ImageAnalysis is YUV, image.planes[0]
				// contains the Y (luminance) plane
				val buffer = image.planes[0].buffer
				// Extract image data from callback object
				val data = buffer.toByteArray()


				// Convert the data into an array of pixel values
				val pixels = data.map { it.toInt() and 0xFF }
				// Compute average luminance for the image
				val luma = pixels.average()
				// Log the new luma value
				Log.d("CameraXApp", "Average luminosity: $luma")
				// Update timestamp of last analyzed frame
				lastAnalyzedTimestamp = currentTimestamp
			}
		}
	}
}
