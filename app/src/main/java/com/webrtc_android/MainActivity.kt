package com.webrtc_android

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.webrtc_android.ActivityExtensions.requestMultiplePermissions
import com.webrtc_android.ActivityExtensions.startPermissionActivity
import com.webrtc_android.databinding.ActivityMainBinding
import org.jetbrains.anko.alert
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var binding: ActivityMainBinding? = null
    private var rootEglBase: EglBase? = null
    private var factory: PeerConnectionFactory? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioConstraints: MediaConstraints? = null
    private var videoConstraints: MediaConstraints? = null
    private var localPeerConnection: PeerConnection? = null
    private var remotePeerConnection: PeerConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        start()
    }

    private fun start() {
        requestMultiplePermissions(listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), {
            initializeSurfaceViews()

            initializePeerConnectionFactory()

            createVideoTrackFromCameraAndShowIt()

            initializePeerConnections()

            startStreamingVideo()
        }, {
            alert(getString(R.string.txt_err_permission_required_message)) {
                title = getString(R.string.txt_err_permission_required_title)

                positiveButton(getString(R.string.txt_give_permission)) {
                    startPermissionActivity()
                    it.dismiss()
                }

                negativeButton(getString(R.string.btn_cancel)) {
                    it.dismiss()
                }
            }.show()
        })
    }

    private fun initializeSurfaceViews() {
        rootEglBase = EglBase.create()

        binding?.localGlSurfaceView?.init(rootEglBase?.eglBaseContext, null)
        binding?.localGlSurfaceView?.setEnableHardwareScaler(true)
        binding?.localGlSurfaceView?.setMirror(true)

        binding?.remoteGlSurfaceView?.init(rootEglBase?.eglBaseContext, null)
        binding?.remoteGlSurfaceView?.setEnableHardwareScaler(true)
        binding?.remoteGlSurfaceView?.setMirror(true)
    }

    private fun initializePeerConnectionFactory() {

        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(rootEglBase?.eglBaseContext,  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true)
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase?.eglBaseContext)
        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory()
    }

    private fun createVideoTrackFromCameraAndShowIt() {
        val videoCapturer: VideoCapturer? = createVideoCapturer()

        //Create a VideoSource instance
        videoCapturer?.let {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase!!.eglBaseContext)
            videoSource = factory?.createVideoSource(videoCapturer.isScreencast)
            videoCapturer.initialize(surfaceTextureHelper, this, videoSource?.capturerObserver)
        }
        localVideoTrack = factory?.createVideoTrack("100", videoSource)

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = MediaConstraints()
        videoConstraints = MediaConstraints()

        //create an AudioSource instance
        audioSource = factory?.createAudioSource(audioConstraints)
        localAudioTrack = factory?.createAudioTrack("101", audioSource)
        videoCapturer?.startCapture(1024, 720, 30)
        binding?.localGlSurfaceView?.visibility = View.VISIBLE
        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        localVideoTrack?.addSink(binding?.localGlSurfaceView)
    }

    private fun initializePeerConnections() {
        localPeerConnection = createPeerConnection(factory, true)
        remotePeerConnection = createPeerConnection(factory, false);
    }

    private fun createPeerConnection(factory: PeerConnectionFactory?, isLocal: Boolean): PeerConnection? {
        val iceServers = ArrayList<PeerConnection.IceServer>()
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers) //new ArrayList<>()
        val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                Log.d(TAG, "onSignalingChange: $signalingState")
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: $iceConnectionState")
            }

            override fun onIceConnectionReceivingChange(b: Boolean) {
                Log.d(TAG, "onIceConnectionReceivingChange: $b")
            }

            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                Log.d(TAG, "onIceGatheringChange:$iceGatheringState")
            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                Log.d(TAG, "onIceCandidate: $iceCandidate")
                if (isLocal) {
                    Log.d(TAG, "isLocal: $isLocal")
                    remotePeerConnection?.addIceCandidate(iceCandidate);
                } else {
                    Log.d(TAG, "isLocal: $isLocal")
                    localPeerConnection?.addIceCandidate(iceCandidate)
                }
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                Log.d(TAG, "onIceCandidatesRemoved: ")
            }

            override fun onAddStream(mediaStream: MediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size)
                gotRemoteStream(mediaStream)
            }

            override fun onRemoveStream(mediaStream: MediaStream) {
                Log.d(TAG, "onRemoveStream: ")
            }

            override fun onDataChannel(dataChannel: DataChannel) {
                Log.d(TAG, "onDataChannel: ")
                Log.d(TAG, "onDataChannel: is local: " + isLocal + " , state: " + dataChannel.state())
            }

            override fun onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ")
            }

            override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
        }
        return factory?.createPeerConnection(rtcConfig, pcObserver)
    }

    private fun startStreamingVideo() {
        //creating local mediastream
        val mediaStream = factory?.createLocalMediaStream("ARDAMS")
        mediaStream?.apply {
            addTrack(localAudioTrack)
            addTrack(localVideoTrack)
            localPeerConnection?.addStream(this)
        }

        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        localPeerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                Log.d(TAG, "onCreateSuccess: ")
                localPeerConnection?.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                remotePeerConnection?.setRemoteDescription(SimpleSdpObserver(), sessionDescription)

                remotePeerConnection?.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                        localPeerConnection?.setRemoteDescription(SimpleSdpObserver(), sessionDescription)
                        remotePeerConnection?.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                    }
                }, sdpMediaConstraints)
            }
        }, sdpMediaConstraints)
    }

    private fun gotRemoteStream(stream: MediaStream) {
        //we have remote video stream. add to the renderer.
        val videoTrack = stream.videoTracks[0]
        runOnUiThread {
            try {
                binding?.remoteGlSurfaceView?.visibility = View.VISIBLE
                videoTrack.addSink(binding?.remoteGlSurfaceView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createVideoCapturer(): VideoCapturer? {
        return if (useCamera2()) {
            createCameraCapturer(Camera2Enumerator(this))
        } else {
            createCameraCapturer(Camera1Enumerator(true))
        }
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(this)
    }
}