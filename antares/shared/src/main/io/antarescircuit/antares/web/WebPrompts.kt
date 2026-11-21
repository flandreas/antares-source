package io.antarescircuit.antares.web

/** Questions to the user that the web application answers asynchronously with its own dialogs. */
internal object WebPrompts {

	/** Asks a yes/no [question] and calls [answer] with the result. Set by [AntaresWebEditor]. */
	var confirm: (question: String, answer: (Boolean) -> Unit) -> Unit = { _, answer -> answer(false) }
}
