package com.zenmanage.kotlin.errors

public class FetchRulesException : ZenmanageException {
    public val statusCode: Int?

    public constructor(message: String) : super(message) {
        statusCode = null
    }

    public constructor(message: String, statusCode: Int?) : super(message) {
        this.statusCode = statusCode
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
        statusCode = null
    }
}
