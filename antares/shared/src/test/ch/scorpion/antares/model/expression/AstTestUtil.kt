package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SyntaxTreePrinter
import kotlin.test.assertEquals

fun assertAST(node: Node, ast: String) {
	val printer = SyntaxTreePrinter()
	node.accept(printer)

	assertEquals(ast, printer.result)
}