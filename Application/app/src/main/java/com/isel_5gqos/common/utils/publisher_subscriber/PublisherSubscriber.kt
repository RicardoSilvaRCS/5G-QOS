package com.isel_5gqos.common.utils.publisher_subscriber

open class MessageEvent

class StringMessageEvent(val message:String): MessageEvent()

class SessionMessageEvent(val sessionState: SessionMessageTypeEnum): MessageEvent()