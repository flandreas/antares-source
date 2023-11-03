package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.parser.TextLocation
import kotlin.test.assertEquals

fun assertAST(node: Node, ast: String) {
	val printer = SyntaxTreePrinter()
	node.accept(printer)

	assertEquals(ast, printer.result)
}

fun assertRowColumn(row: Int, column: Int, location: TextLocation) {
	assertEquals(row, location.row, "Wrong row")
	assertEquals(column, location.column, "Wrong column")
}