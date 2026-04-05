package io.antarescircuit.jabbah.base.parser

import io.antarescircuit.jabbah.base.Issue
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.ScriptMetaData
import io.antarescircuit.jabbah.base.dsl.SyntaxError
import io.antarescircuit.jabbah.base.event.EventBus

interface Parser {

	/**
	 * Parses the program this [Parser] was created with and returns the corresponding AST.
	 * @throws SyntaxError if the sentence is syntactically invalid
	 */
	fun parse(): Node

	/**
	 * Calls [parse] and catches [DslError] by posting an [Issue] on the system's [EventBus].
	 * @param metaData used for describing the [Issue]
	 */
	fun parseCatching(metaData: ScriptMetaData): Node?
}