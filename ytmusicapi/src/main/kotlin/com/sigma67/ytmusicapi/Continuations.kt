package com.sigma67.ytmusicapi

/**
 * Continuation utilities for handling paginated API responses
 */

val CONTINUATION_TOKEN =
    listOf("continuationItemRenderer", "continuationEndpoint", "continuationCommand", "token")
val CONTINUATION_ITEMS =
    listOf("onResponseReceivedActions", 0, "appendContinuationItemsAction", "continuationItems")

/**
 * Retrieve the continuation token from a result list
 */
fun getContinuationToken(results: JsonList): String? {
    @Suppress("UNCHECKED_CAST")
    val lastItem = results.lastOrNull() ?: return null
    return navigatePath(lastItem, CONTINUATION_TOKEN) as? String
}

/**
 * Navigate a path through nested JSON structures
 */
fun navigatePath(data: JsonDict?, path: List<Any>, returnNullIfNotFound: Boolean = false): Any? {
    var current: Any? = data

    for (key in path) {
        when (current) {
            is Map<*, *> if key is String -> {
                @Suppress("UNCHECKED_CAST")
                current = (current[key] as JsonDict)
            }

            is List<*> if key is Int -> {
                current = (if (key >= 0 && key < current.size) current[key] else null)
            }

            else -> {
                return if (returnNullIfNotFound) null else current
            }
        }

        if (current == null) {
            return null
        }
    }

    return current
}

/**
 * Get continuation data from API response
 */
fun getContinuationData(response: JsonDict): JsonList? {
    @Suppress("UNCHECKED_CAST")
    return navigatePath(response, CONTINUATION_ITEMS) as? JsonList
}

/**
 * Create continuation request parameters
 */
fun getContinuationParams(results: JsonDict, ctokenPath: String = ""): JsonDict {
    val continuation = getKeyValue(results, "continuations") as? List<*>
    val firstContinuation = continuation?.firstOrNull() as? JsonDict ?: return emptyMap()

    return firstContinuation.toMutableMap() as MutableJsonDict
}

/**
 * Get reloadable continuation parameters (used for playlists)
 */
fun getReloadableContinuationParams(results: JsonDict): String {
    val path = listOf(
        "continuations",
        0,
        "reloadContinuationItemRenderer",
        "continuationEndpoint",
        "reloadContinuationCommand",
        "reloadContinuationData",
        "reloadUiTypes"
    )
    val reloadUiTypes = navigatePath(results, path) as? List<String> ?: emptyList()

    // Encode as query parameter
    return reloadUiTypes.joinToString(",")
}

/**
 * Retrieve a nested value from a JSON structure using a key path
 */
fun getKeyValue(data: Any?, vararg keys: String): Any? {
    var current = data
    for (key in keys) {
        if (current is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            current = (current as JsonDict)[key]
        } else {
            return null
        }
    }
    return current
}

/**
 * Flatten a list of continuation results with parsing
 */
fun handleContinuations(
    initialResults: JsonDict,
    continuationType: String,
    limit: Int? = null,
    requestFunc: (String) -> JsonDict,
    parseFunc: (JsonList) -> JsonList,
    ctokenPath: String = "",
): JsonList {
    val items = mutableListOf<JsonDict>()
    var results = initialResults

    while ("continuations" in results && (limit == null || items.size < limit)) {
        val continuationData = getContinuationData(results) ?: break

        val parsed = parseFunc(continuationData)
        if (parsed.isEmpty()) break
        items.addAll(parsed)

        // Get next continuation token for next iteration
        val token = getContinuationToken(continuationData) ?: break

        // Make next request
        val response = requestFunc(token)
        if (response.containsKey("continuationContents")) {
            @Suppress("UNCHECKED_CAST")
            results =
                (response["continuationContents"] as? JsonDict)?.get(continuationType) as? JsonDict
                    ?: break
        } else {
            break
        }
    }

    return items
}
