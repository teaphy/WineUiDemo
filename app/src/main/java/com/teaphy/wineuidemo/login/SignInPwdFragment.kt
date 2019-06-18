package com.teaphy.wineuidemo.login


import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs

import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.fragment_sign_in_pwd.*

class SignInPwdFragment : Fragment() {


	private val email: String by lazy {
		arguments?.getString("email") ?: ""
	}
	private val loginViewModel: LoginViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_sign_in_pwd, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		forget_pwd_text.paintFlags = Paint.UNDERLINE_TEXT_FLAG
		email_text.text = getString(R.string.password_fo_email, email)

		loginViewModel.stepStatusLiveData.observe(this, Observer {
			it?.apply {
				if (this == LoginStepType.LOGIN) {
					Toast.makeText(context, "do login", Toast.LENGTH_SHORT).show()
				}
			}
		})
	}

}
