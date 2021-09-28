package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*

class GraphDslSemanticAnalyser(
	symbolTable: SymbolTable?
) : SemanticAnalyser(symbolTable) {

	private var inInit = false
	private var initVisited = false

	override fun createVisitor(): HierarchyVisitor = GraphDslVisitor()

	override val allowStoreDeclaration: Boolean
		get() = super.allowStoreDeclaration || inInit

	private open inner class GraphDslVisitor : Visitor() {
		override fun visitEnter(node: Any): Boolean {
			when (node) {
				is InitStatement -> visitInitStatement(node)
				else -> super.visitEnter(node)
			}
			return true
		}

		override fun visitLeave(node: Any): Boolean {
			inInit = false
			return super.visitLeave(node)
		}
	}

	private fun visitInitStatement(node: InitStatement) {
		inInit = true
		if (initVisited) {
			throw SemanticError(node.location, Translations.getString("graph.dsl.atMostOneInitBlock.msg"))
		}
		if (scope.scopeLevel > 1) {
			throw SemanticError(node.location, Translations.getString("graph.dsl.unexpectedInitBlock.msg"))
		}
		initVisited = true
	}
}