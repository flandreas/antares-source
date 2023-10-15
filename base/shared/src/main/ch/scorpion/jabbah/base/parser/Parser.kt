package ch.scorpion.jabbah.base.parser

import ch.scorpion.jabbah.base.Issue
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.ScriptMetaData
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.event.EventBus

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