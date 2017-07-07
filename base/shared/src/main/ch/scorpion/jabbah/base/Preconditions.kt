package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException

/**
 * Cross target versions of Kotlin's check methods to be used for ensuring preconditions.
 */

fun checkArgument(cond: Boolean, msg: String? = null) {
    if (!cond) {
        if (msg != null) {
            throw IllegalArgumentException("Illegal argument: ${msg}")
        }
        throw IllegalArgumentException("Illegal argument")
    }
}

fun checkState(cond: Boolean, msg: String? = null) {
    if (!cond) {
        if (msg != null) {
            throw IllegalStateException("Illegal state: ${msg}")
        }
        throw IllegalStateException("Illegal state")
    }
}

