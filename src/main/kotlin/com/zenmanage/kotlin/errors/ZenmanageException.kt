package com.zenmanage.kotlin.errors

public open class ZenmanageException : RuntimeException {
    public constructor(message: String) : super(message)

    public constructor(message: String, cause: Throwable) : super(message, cause)
}
