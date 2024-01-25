package ch.scorpion.jabbah.base

import kotlin.js.JsExport

/**
 * Universal unique ID.
 */
@JsExport
data class UUID(val id: String) {
    override fun toString() = id
}