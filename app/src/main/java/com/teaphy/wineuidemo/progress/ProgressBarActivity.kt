package com.teaphy.wineuidemo.progress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.activity_progress_bar.*

@Route(path = "/account/progressBar")
class ProgressBarActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_progress_bar)

		seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				val bias: Float = (progress / 100.00).toFloat()
				with(pro_text) {
					text = progress.toString()
					val params = layoutParams as ConstraintLayout.LayoutParams
					params.horizontalBias = bias
					layoutParams = params
				}
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {

			}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {

			}

		})
	}
}
