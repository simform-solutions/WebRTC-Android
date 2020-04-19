package com.webrtc_android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.webrtc_android.ActivityExtensions.launchActivity

class StartActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun startPeerToPeer(view: View){
        launchActivity<MainActivity>()
    }

    fun startSampleSocket(){

    }
}