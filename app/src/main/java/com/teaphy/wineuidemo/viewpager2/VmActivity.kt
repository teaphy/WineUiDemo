package com.teaphy.wineuidemo.viewpager2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.activity_vm.*

@Route(path = "/account/viewpager2")
class VmActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_vm)

		initBanner()
	}

	private fun initBanner() {
		val list = listOf<String>(
			"http://pic40.nipic.com/20140331/9469669_142840860000_2.jpg",
			"http://pic25.nipic.com/20121205/10197997_003647426000_2.jpg",
			"http://pic15.nipic.com/20110628/1369025_192645024000_2.jpg",
			"http://pic40.nipic.com/20140331/9469669_142840860000_2.jpg"
		)

		val bannerAdapter = BannerAdapter(list)

		with(banner_vp) {
			adapter = bannerAdapter

			// 设置页面转换动画
			setPageTransformer(ZoomOutPageTransformer())

			// 设置预加载
//			offscreenPageLimit = 20
		}
	}



	class ZoomOutPageTransformer : ViewPager2.PageTransformer {
		private  val MIN_SCALE = 0.85f
		private  val MIN_ALPHA = 0.5f

		override fun transformPage(view: View, position: Float) {
			view.apply {
				val pageWidth = width
				val pageHeight = height
				when {
					position < -1 -> { // [-Infinity,-1)
						// This page is way off-screen to the left.
						alpha = 0f
					}
					position <= 1 -> { // [-1,1]
						// Modify the default slide transition to shrink the page as well
						val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
						val vertMargin = pageHeight * (1 - scaleFactor) / 2
						val horzMargin = pageWidth * (1 - scaleFactor) / 2
						translationX = if (position < 0) {
							horzMargin - vertMargin / 2
						} else {
							horzMargin + vertMargin / 2
						}

						// Scale the page down (between MIN_SCALE and 1)
						scaleX = scaleFactor
						scaleY = scaleFactor

						// Fade the page relative to its size.
						alpha = (MIN_ALPHA +
								(((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
					}
					else -> { // (1,+Infinity]
						// This page is way off-screen to the right.
						alpha = 0f
					}
				}
			}
		}
	}
}
