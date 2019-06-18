package com.teaphy.wineuidemo.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.fragment_sign_in_email.*


class SignInEmailFragment : Fragment() {

	val loginViewModel: LoginViewModel by activityViewModels()


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_sign_in_email, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		email_edit.addTextChangedListener(object : TextWatcher{
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

			}

			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
			}

			override fun afterTextChanged(s: Editable?) {
				if (null == s) {
					setEMailError()
				}

				s?.apply {
					if (s.isEmpty()) {
						setEMailError()
					}

					val eMail = s.toString()
					Log.e("teaphy", "justifyEmail(eMail): ${justifyEmail(eMail)}, eMail: $eMail")
					if (justifyEmail(eMail)) {
						email_edit_layout.isErrorEnabled = false
					} else {
						setEMailError()
					}
				}
			}
		})

		loginViewModel.stepStatusLiveData.observe(this, Observer {
			it?.apply {
				if (this == LoginStepType.PASSWORD) {
					navToPassword()
				}
			}
		})
	}

	fun setEMailError() {
		with(email_edit_layout) {
			error = "please input email"
			isErrorEnabled = true
		}
	}

	fun justifyEmail(email: String): Boolean {
		val emPattern = "^([0-9A-Za-z\\-_\\.]+)@([0-9a-z]+\\.[a-z]{2,3}(\\.[a-z]{2})?)$"
		return Regex(emPattern).matches(email)
	}

	private fun navToPassword() {

		val email = email_edit.text.toString()

		if (email.isNotEmpty()) {
			findNavController().navigate(R.id.action_signInEmailFragment_to_signInPwdFragment, bundleOf("email" to  email))
		} else {
			Toast.makeText(context, "please input email", Toast.LENGTH_SHORT).show()
		}

	}
}
