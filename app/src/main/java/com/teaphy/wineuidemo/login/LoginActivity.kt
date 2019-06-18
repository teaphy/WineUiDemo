package com.teaphy.wineuidemo.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.alibaba.android.arouter.facade.annotation.Route
import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.activity_login.*

@Route(path = "/account/login")
class LoginActivity : AppCompatActivity() {

	private val loginViewModel: LoginViewModel by lazy {
		ViewModelProviders.of(this).get(LoginViewModel::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		next_text.setOnClickListener {
			doNext()
		}

		// 监听导航destination变化
		findNavController(R.id.login_nav_host_fragment).addOnDestinationChangedListener { _, destination, _ ->
			updateNextDisplay(destination.id != R.id.signInFragment)
			loginViewModel.loginStepType = when (destination.id) {
				R.id.signInFragment -> LoginStepType.MANUAL
				R.id.signInEmailFragment -> LoginStepType.EMAIL
				R.id.signInPwdFragment -> LoginStepType.PASSWORD
				else -> LoginStepType.MANUAL
			}
		}
	}

	private fun doNext() {
		when (loginViewModel.loginStepType) {
			LoginStepType.EMAIL -> loginViewModel.updateStepStatus(LoginStepType.PASSWORD)
			LoginStepType.PASSWORD -> loginViewModel.updateStepStatus(LoginStepType.LOGIN)
		}
	}


	private fun updateNextDisplay(isDisplay: Boolean) {
		next_text.visibility = if (isDisplay) {
			View.VISIBLE
		} else {
			View.GONE
		}
	}
}
