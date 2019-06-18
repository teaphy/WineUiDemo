package com.teaphy.wineuidemo.login


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.teaphy.wineuidemo.R
import kotlinx.android.synthetic.main.fragment_sign_in.*


class SignInFragment : Fragment() {

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_sign_in, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		sign_in_text.setOnClickListener {

			findNavController().navigate(R.id.action_signInFragment_to_signInEmailFragment)
		}
	}

}
