package com.teaphy.wineuidemo.app

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils

class WineUiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 工具类初始化
        Utils.init(this)

        // 初始化ARouter
        // 打印日志
        ARouter.openLog()
        // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        ARouter.openDebug()
        ARouter.init(this)
    }
}