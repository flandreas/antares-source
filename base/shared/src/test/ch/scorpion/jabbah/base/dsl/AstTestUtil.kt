package ch.scorpion.jabbah.base.dsl

import kotlin.test.assertEquals

fun assertAST(node: Node, ast: String) {
	val printer = SyntaxTreePrinter()
	node.accept(printer)

	assertEquals(ast, printer.result)
}