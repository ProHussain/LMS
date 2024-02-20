package com.hazel.lms.data.sealed

sealed class RequestResult<T> {
    class Idle<T> : RequestResult<T>()
    class Loading<T> : RequestResult<T>()
    data class Success<T>(val data: T,val message:String = "") : RequestResult<T>()
    data class Error<T>(val message: String) : RequestResult<T>()
}