package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.execution.issue.Issue

/**
 * Represents a script to be executed by [ScriptEngine].
 * In addition to the script code, it contains information about the origin of the script,
 * and the context in which it is execution. This is usefully when raising [Issue]s in case of an error.
 *
 * @property code the script code to be executed
 * @property origin information about the object that requests script execution
 * @property context additional information to further identify the origin of the script
 */
data class Script(
        val code: String,
        val origin: String,
        val context: String? = null
)