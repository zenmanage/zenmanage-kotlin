package com.zenmanage.kotlin.config

public class NullLogger : Logger {
    override fun debug(message: String): Unit = Unit

    override fun info(message: String): Unit = Unit

    override fun warn(message: String): Unit = Unit

    override fun error(message: String, throwable: Throwable): Unit = Unit
}
