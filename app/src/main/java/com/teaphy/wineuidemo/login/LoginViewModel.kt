package com.teaphy.wineuidemo.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

	var loginStepType: LoginStepType = LoginStepType.MANUAL

	val stepStatusLiveData: MutableLiveData<LoginStepType> by lazy {
		MutableLiveData<LoginStepType>()
	}

	fun updateStepStatus(loginStepType: LoginStepType) {
		stepStatusLiveData.value = loginStepType
	}
}