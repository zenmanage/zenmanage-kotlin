package com.zenmanage.kotlin.config

public interface Logger {
    public fun debug(message: String)

    public fun info(message: String)

    public fun warn(message: String)

    public fun error(message: String, throwable: Throwable)
}
