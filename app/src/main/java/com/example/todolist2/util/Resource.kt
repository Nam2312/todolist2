package com.example.todolist2.util

/**
 * A generic class that holds a value with its loading status.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

fun <T> Resource<T>.data(): T? = when (this) {
    is Resource.Success -> data
    else -> null
}

fun <T> Resource<T>.isLoading(): Boolean = this is Resource.Loading

fun <T> Resource<T>.isSuccess(): Boolean = this is Resource.Success

fun <T> Resource<T>.isError(): Boolean = this is Resource.Error

fun <T> Resource<T>.errorMessage(): String? = when (this) {
    is Resource.Error -> message
    else -> null
}







