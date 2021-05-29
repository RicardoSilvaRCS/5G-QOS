package com.isel_5gqos.utils.publisher_subscriber

open class MessageEvent

class StringMessageEvent(val message:String):MessageEvent()

class SessionMessageEvent(val sessionState:SessionMessageTypeEnum):MessageEvent()