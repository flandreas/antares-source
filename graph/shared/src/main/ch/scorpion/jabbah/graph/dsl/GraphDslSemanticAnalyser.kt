package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.SemanticError

class GraphDslSemanticAnalyser(
	symbolTable: ScopedSymbolTable?
) : SemanticAnalyser(symbolTable) {

	private var initVisited = false

	override fun createVisitor(): HierarchyVisitor = GraphDslVisitor()

	private open inner class GraphDslVisitor : Visitor() {
		override fun visitEnter(node: Any): Boolean {
			when (node) {
				is InitStatement -> visitInitStatement(node)
				else -> super.visitEnter(node)
			}
			return true
		}
	}

	private fun visitInitStatement(node: InitStatement) {
		if (initVisited) {
			throw SemanticError(node.location, Translations.getString("graph.dsl.atMostOneInitBlock.msg"))
		}
		if (scope.level > 1) {
			throw SemanticError(node.location, Translations.getString("graph.dsl.unexpectedInitBlock.msg"))
		}
		initVisited = true
	}
}