package com.foodsnap.util

/**
 * A sealed class that represents the state of a data operation.
 *
 * This is used throughout the app to wrap data from repositories and use cases,
 * allowing the UI to react appropriately to loading, success, and error states.
 *
 * @param T The type of data being wrapped
 * @property data The data payload (available in Success and optionally in Error for cached data)
 * @property message An optional message, typically used for error descriptions
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * Represents a loading state, optionally with cached data.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * Represents a successful operation with data.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents an error state with a message and optionally cached data.
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

/**
 * Extension function to map Resource data from one type to another.
 *
 * @param transform The transformation function to apply to the data
 * @return A new Resource with the transformed data
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Loading -> Resource.Loading(data?.let(transform))
        is Resource.Success -> Resource.Success(transform(data!!))
        is Resource.Error -> Resource.Error(message ?: "Unknown error", data?.let(transform))
    }
}

/**
 * Extension function to perform an action when Resource is Success.
 */
inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        action(data!!)
    }
    return this
}

/**
 * Extension function to perform an action when Resource is Error.
 */
inline fun <T> Resource<T>.onError(action: (String) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        action(message ?: "Unknown error")
    }
    return this
}

/**
 * Extension function to perform an action when Resource is Loading.
 */
inline fun <T> Resource<T>.onLoading(action: () -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        action()
    }
    return this
}
