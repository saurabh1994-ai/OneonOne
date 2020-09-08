package com.sws.oneonone.model
//Cloud Flare video url model

class CFVideoUrlModel(
    val result: resultModel

)

class resultModel(
    val thumbnail:String,
    val playback: playbackmodel
)

class playbackmodel(
    val  hls:String
)



