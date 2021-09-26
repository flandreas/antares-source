package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Issue

/**
 * Represents the metadata of a DSL script.
 * It contains information about the origin of the script, and the context in which it is execution.
 * This is usefully when raising [Issue]s in case of an error.
 *
 * @property origin information about the object that requests script execution
 * @property context additional information to further identify the origin of the script
 */
data class ScriptMetaData(
	val origin: String,
	val context: String
)