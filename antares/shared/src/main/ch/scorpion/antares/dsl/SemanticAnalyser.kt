package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.EmptyHierarchyVisitor

/**
 * Performs semantic analysis on an Abstract Syntax Tree and produces
 * a [ScopedSymbolTable] as a side effect.
 *
 * Use it as [HierarchyVisitor] by [Node.accept(analyser)].
 *
 * @throws SemanticError for semantic errors found during analysis
 */
class SemanticAnalyser : EmptyHierarchyVisitor() {

	var scope: ScopedSymbolTable = ScopedSymbolTable("global", level = 1, enclosingScope = null)
		private set

	private var currentlyDeclaredVariableName: String? = null

	override fun visitEnter(node: Any): Boolean {
		when (node) {
			is Declaration -> {
				currentlyDeclaredVariableName = node.left.token.value
				enterDeclaration(node)
			}
			is Assignment -> {
				currentlyDeclaredVariableName = node.left.token.value
				enterAssignment(node)
			}
			is Block -> enterBlock(node)
		}
		return true
	}

	override fun visit(node: Any): Boolean {
		when (node) {
			is Variable -> variable(node)
		}
		return true
	}

	override fun visitLeave(node: Any): Boolean {
		when (node) {
			is Declaration -> currentlyDeclaredVariableName = null
			is Assignment -> currentlyDeclaredVariableName = null
			is Block -> leaveBlock(node)
		}
		return true
	}

	private fun enterDeclaration(declaration: Declaration) {
		val varName = declaration.left.token.value as String
		val typeSymbol = null as BuiltInTypeSymbol?
		val varSymbol = VariableSymbol(varName, typeSymbol)
		if (scope.lookup(varName, currentScopeOnly = true) != null) {
			throw SemanticError(declaration.location, "Variable '$varName' already declared")
		}
		scope.define(varSymbol)
	}

	private fun enterAssignment(assignment: Assignment) {
		// If implicit declaration wouldn't be supported, we would check here
		// whether the variable is already declared, and if not, throw an error
		val varName = assignment.left.token.value as String
		val typeSymbol = null as BuiltInTypeSymbol?
		if (scope.lookup(varName) == null) {
			val varSymbol = VariableSymbol(varName, typeSymbol)
			scope.define(varSymbol)
		}
	}

	private fun enterBlock(block: Block) {
		scope = ScopedSymbolTable("block", level = scope.level + 1, enclosingScope = scope)
	}

	private fun leaveBlock(block: Block) {
		scope.enclosingScope?.let { scope = it }
	}

	private fun variable(variable: Variable) {
		val name = variable.token.value as String
		if (scope.lookup(name) == null && currentlyDeclaredVariableName != name) {
			throw SemanticError(variable.location, "Variable '$name' not defined")
		}
	}
}