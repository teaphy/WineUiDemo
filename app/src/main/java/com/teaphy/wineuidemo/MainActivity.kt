package com.teaphy.wineuidemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

	public fun doOnClick(view: View) {
		when (view.id) {
			R.id.login_button -> goToLogin()
			R.id.start_camera_button -> goToTakePhoto()
			R.id.progress_bar_button -> goProgressBar()
			R.id.scan_button -> goScan()
			R.id.view_pager_button -> goViewPager()
		}
	}

	private fun goViewPager() {
		ARouter.getInstance()
			.build("/account/viewpager2")
			.navigation()
	}

	private fun goScan() {
		ARouter.getInstance()
			.build("/account/scan")
			.navigation()
	}

	private fun goProgressBar() {
		ARouter.getInstance()
			.build("/account/progressBar")
			.navigation()
	}

	private fun goToTakePhoto() {
		ARouter.getInstance()
			.build("/account/takePhoto")
			.navigation()
	}

	private fun goToLogin() {
		ARouter.getInstance()
			.build("/account/login")
			.navigation()
	}
}
