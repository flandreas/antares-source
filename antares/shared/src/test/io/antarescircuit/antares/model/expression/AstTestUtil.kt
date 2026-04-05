package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.SyntaxTreePrinter
import kotlin.test.assertEquals

fun assertAST(node: Node, ast: String) {
	val printer = SyntaxTreePrinter()
	node.accept(printer)

	assertEquals(ast, printer.result)
}