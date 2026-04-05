package io.antarescircuit.jabbah.edit.auth

/**
 * An [Operation] is executed by the [User] on the current data if he is authorized to do so.
 */
enum class Operation {
	View,
	Change,
	Execute
}