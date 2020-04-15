package com.webrtc_android

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SimpleSdpObserver : SdpObserver {
    override fun onSetFailure(p0: String?) {
    }

    override fun onSetSuccess() {
    }

    override fun onCreateSuccess(sessionDescription: SessionDescription?) {
    }

    override fun onCreateFailure(p0: String?) {
    }
}