package com.teaphy.wineuidemo.scan

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.app.ActivityCompat
import com.teaphy.wineuidemo.R

/**
 *
 * 此视图覆盖在相机预览的顶部。
 * 它在其外部添加了取景器矩形。
 *
 */
class ViewfinderView// This constructor is used when the class is built from an XML resource.
	(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

	private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private val traAnglePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private var resultBitmap: Bitmap? = null
	private val maskColor: Int
	private val resultColor: Int
	private var scannerAlpha: Int = 0
	private val triAngleLength = dp2px(20) //每个角的点距离
	private val triAngleWidth = 12 //每个角的点宽度
	private var triAngleColor: Int = Color.parseColor("#76EE00")
		set(value) {
			traAnglePaint.color = value
			field = value
		}

	private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

	private var lineOffsetCount = 0

	var frameFocus: Rect? = null

	private var widthMeasure: Int = 0
	private var heightMeasure: Int = 0

	init {

		// Initialize these once for performance rather than calling them every time in onDraw().

		// 初始化traAnglePaint
		with(traAnglePaint) {
			color = triAngleColor
			strokeWidth = triAngleWidth.toFloat()
			style = Paint.Style.STROKE
		}

		maskColor = getColor(R.color.viewfinder_mask)
		resultColor = getColor(R.color.result_view)
		scannerAlpha = 0
	}

	@SuppressLint("DrawAllocation")
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		widthMeasure =  MeasureSpec.getSize(widthMeasureSpec)
		heightMeasure = MeasureSpec.getSize(heightMeasureSpec)
		val rectSide = Math.min(widthMeasure, heightMeasure) * 2 / 3
		frameFocus = Rect(widthMeasure / 2 - rectSide/2,
			heightMeasure / 2 - rectSide/2,
			widthMeasure / 2 + rectSide/2 ,
			heightMeasure / 2 + rectSide/2)

		Log.e("teaphy", "frameFocus: $frameFocus, rectSide: $rectSide")
		Log.e("teaphy", "widthMeasure: $widthMeasure, heightMeasure: $heightMeasure")
	}

	@SuppressLint("DrawAllocation")
	public override fun onDraw(canvas: Canvas) {

		if (null == frameFocus) {
			return
		}

		// Draw the exterior (i.e. outside the framing rect) darkened
		val topY = (heightMeasure - frameFocus!!.height() - triAngleWidth) / 2
		val bottomY = (heightMeasure + frameFocus!!.height() + triAngleWidth) / 2
		paint.color = if (resultBitmap != null) resultColor else maskColor
		canvas.drawRect(
			0f,
			0f,
			widthMeasure.toFloat(),
			(heightMeasure - frameFocus!!.height()).shr(1).toFloat() - triAngleWidth, paint
		)
		canvas.drawRect(
			0f,
			((heightMeasure - frameFocus!!.height()).shr(1)).toFloat() - triAngleWidth,
			(widthMeasure - frameFocus!!.width()).toFloat().div(2) - triAngleWidth,
			((heightMeasure + frameFocus!!.height()).shr(1) + triAngleWidth).toFloat(), paint
		)
		canvas.drawRect(
			((widthMeasure + frameFocus!!.width()).shr(1) + triAngleWidth).toFloat(),
			((heightMeasure - frameFocus!!.height()).shr(1)).toFloat() - triAngleWidth,
			widthMeasure.toFloat(),
			((heightMeasure + frameFocus!!.height()).shr(1) + triAngleWidth).toFloat(), paint
		)
		canvas.drawRect(
			0f,
			(heightMeasure + frameFocus!!.height()).shr(1).toFloat() + triAngleWidth,
			widthMeasure.toFloat(),
			heightMeasure.toFloat(), paint
		)

		// 四个角落的三角
		val leftTopPath = Path()
		leftTopPath.moveTo((frameFocus!!.left + triAngleLength).toFloat(), topY.toFloat())
		leftTopPath.lineTo((widthMeasure - frameFocus!!.width() - triAngleWidth).shr(1).toFloat(), topY.toFloat())
		leftTopPath.lineTo((widthMeasure - frameFocus!!.width() - triAngleWidth).shr(1).toFloat(), (topY + triAngleLength).toFloat())
		canvas.drawPath(leftTopPath, traAnglePaint)

		val rightTopPath = Path()
		rightTopPath.moveTo((frameFocus!!.right - triAngleLength).toFloat(), topY.toFloat())
		rightTopPath.lineTo((widthMeasure + frameFocus!!.width() + triAngleWidth).shr(1).toFloat(), topY.toFloat())
		rightTopPath.lineTo((widthMeasure + frameFocus!!.width() + triAngleWidth).shr(1).toFloat(), (topY + triAngleLength).toFloat())
		canvas.drawPath(rightTopPath, traAnglePaint)

		val leftBottomPath = Path()
		leftBottomPath.moveTo(
			(widthMeasure - frameFocus!!.width() - triAngleWidth).shr(1).toFloat(),
			(bottomY - triAngleLength).toFloat()
		)
		leftBottomPath.lineTo((widthMeasure - frameFocus!!.width() - triAngleWidth).shr(1).toFloat(), bottomY.toFloat())
		leftBottomPath.lineTo((frameFocus!!.left + triAngleLength).toFloat(), bottomY.toFloat())
		canvas.drawPath(leftBottomPath, traAnglePaint)

		val rightBottomPath = Path()
		rightBottomPath.moveTo((frameFocus!!.right - triAngleLength).toFloat(), bottomY.toFloat())
		rightBottomPath.lineTo((widthMeasure + frameFocus!!.width() + triAngleWidth).shr(1).toFloat(), bottomY.toFloat())
		rightBottomPath.lineTo(
			(widthMeasure + frameFocus!!.width() + triAngleWidth).shr(1).toFloat(),
			(bottomY - triAngleLength).toFloat()
		)
		canvas.drawPath(rightBottomPath, traAnglePaint)


		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.alpha = CURRENT_POINT_OPACITY
			canvas.drawBitmap(resultBitmap!!, null, frameFocus, paint)
		} else {
			//循环划线，从上到下
			if (lineOffsetCount > frameFocus!!.bottom - frameFocus!!.top - dp2px(10)) {
				lineOffsetCount = 0
			} else {
				lineOffsetCount += 2
				// 移动的线
				val lineRect = Rect()
				lineRect.left = frameFocus!!.left
				lineRect.top = frameFocus!!.top + lineOffsetCount
				lineRect.right = frameFocus!!.right
				lineRect.bottom = frameFocus!!.top + dp2px(2) + lineOffsetCount
				val scanLine = (ActivityCompat.getDrawable(context, R.mipmap.afcs_ic_scan_line)) as BitmapDrawable
				canvas.drawBitmap(scanLine.bitmap, null, lineRect, linePaint)
			}

			postInvalidateDelayed(10L, frameFocus!!.left, frameFocus!!.top, frameFocus!!.right, frameFocus!!.bottom)

		}
	}

	companion object {
		private val CURRENT_POINT_OPACITY = 0xA0
	}

	@ColorInt
	private fun getColor(@ColorRes colorRes: Int): Int {
		return ActivityCompat.getColor(context, colorRes)
	}

	private fun dp2px(dp: Int): Int {
		val density = context.resources.displayMetrics.density
		return (dp * density + 0.5f).toInt()
	}
}