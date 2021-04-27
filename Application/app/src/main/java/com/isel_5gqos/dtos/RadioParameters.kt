package com.isel_5gqos.dtos

class RadioParameters (
    val no : Int,
    val tech : String,
    val arfcn : Int,  //Absolute Radio-Frequency Channel Number
    val rssi : Float,   //Received signal strength indication
    val rsrp : Float,   //Reference signal receive power
    val cId : Int,    //Carrier id
    val psc : Int,    //Primary scrambling controller
    val pci : Float,    //Primary cell Identity
    val rssnr : Float,  //Reference Signal Signal-to-noise Ratio
    val rsqr : Float,   //Reference Signal Received Quality
    val netDataType : String
)
