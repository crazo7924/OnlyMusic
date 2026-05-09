package com.sigma67.ytmusicapi

/**
 * Type alias for JSON objects represented as Map<String, Any?>
 */
typealias JsonDict = Map<String, Any?>

/**
 * Type alias for JSON lists of JSON objects
 */
typealias JsonList = List<JsonDict>

/**
 * Mutable version of JsonDict
 */
typealias MutableJsonDict = MutableMap<String, Any?>

/**
 * Mutable version of JsonList
 */
typealias MutableJsonList = MutableList<JsonDict>

/**
 * Function type for making requests that return JsonDict
 */
typealias RequestFuncType = (String) -> JsonDict

/**
 * Function type for request processing that transforms JsonDict
 */
typealias RequestFuncBodyType = (JsonDict) -> JsonDict

/**
 * Function type for parsing that transforms JsonList
 */
typealias ParseFuncType = (JsonList) -> JsonList

/**
 * Function type for parsing that transforms JsonDict
 */
typealias ParseFuncDictType = (JsonDict) -> JsonDict
