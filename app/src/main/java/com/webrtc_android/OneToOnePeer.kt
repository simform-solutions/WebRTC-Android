package com.webrtc_android

class OneToOnePeer {

    /*private var socket: Socket? = null
    private var isInitiator = false
    private var isChannelReady = false
    private val isStarted = false*/

    /*
    private fun connectToSignallingServer() {
        try {
            socket = IO.socket("https://salty-sea-26559.herokuapp.com/")
            socket?.on(EVENT_CONNECT) {
                Log.d(TAG, "connectToSignallingServer: connect")
                socket?.emit("create or join", "foo")
            }?.on("ipaddr") {
                Log.d(TAG, "connectToSignallingServer: ipaddr")
            }?.on("created") {
                Log.d(TAG, "connectToSignallingServer: created")
                isInitiator = true
            }?.on("full") {
                Log.d(TAG, "connectToSignallingServer: full")
            }?.on("join") {
                Log.d(TAG, "connectToSignallingServer: join")
                Log.d(TAG, "connectToSignallingServer: Another peer made a request to join room")
                Log.d(TAG, "connectToSignallingServer: This peer is the initiator of room")
                isChannelReady = true
            }?.on("joined") {
                Log.d(TAG, "connectToSignallingServer: joined")
                isChannelReady = true
            }?.on("log") { args ->
                for (arg in args) {
                    Log.d(TAG, "connectToSignallingServer: $arg")
                }
            }?.on("message") {
                Log.d(TAG, "connectToSignallingServer: got a message")
            }?.on("message") { args ->
                try {
                    if (args.get(0) is String) {
                        val message = args.get(0) as String?
                        if (message == "got user media") {
                            maybeStart()
                        }
                    } else {
                        val message = args.get(0) as JSONObject?
                        Log.d(TAG, "connectToSignallingServer: got message $message")
                        if (message!!.getString("type") == "offer") {
                            Log.d(TAG, "connectToSignallingServer: received an offer $isInitiator $isStarted")
                            if (!isInitiator && !isStarted) {
                                maybeStart()
                            }
                            peerConnection.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.OFFER, message.getString("sdp")))
                            doAnswer()
                        } else if (message.getString("type") == "answer" && isStarted) {
                            peerConnection.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, message.getString("sdp")))
                        } else if (message.getString("type") == "candidate" && isStarted) {
                            Log.d(TAG, "connectToSignallingServer: receiving candidates")
                            val candidate = IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"))
                            peerConnection.addIceCandidate(candidate)
                        }
                        /*else if (message === 'bye' && isStarted) {
                            handleRemoteHangup();
                        }*/
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }?.on(EVENT_DISCONNECT) {
                Log.d(TAG, "connectToSignallingServer: disconnect")
            }
            socket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
    * */
}