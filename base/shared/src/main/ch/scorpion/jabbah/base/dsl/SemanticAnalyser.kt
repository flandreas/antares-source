package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations

fun interface SemanticAnalyserFactory {
	fun create(symbolTable: SymbolTable?): SemanticAnalyser
}

/**
 * Performs semantic analysis on an Abstract Syntax Tree and produces
 * a [ScopedSymbolTable] as a side effect.
 *
 * @throws SemanticError for semantic errors found during analysis
 */
open class SemanticAnalyser(
	context: SymbolTable?
) {

	// Visible for testing
	var scope: SymbolTable = ScopedSymbolTable("global", scopeLevel = 1, enclosingScope = context)
		private set

	private val visitor = createVisitor()

	/**
	 * Analyses the Abstract Syntax Tree in [program].
	 * @throws SemanticError in case of a semantic error
	 */
	fun analyse(program: Node) {
		program.accept(visitor)
	}

	protected open fun createVisitor(): HierarchyVisitor = Visitor()

	private var currentlyDeclaredVariableName: String? = null

	protected open inner class Visitor : EmptyHierarchyVisitor() {

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
				is Block -> enterBlock()
				is ForStatement -> {
					currentlyDeclaredVariableName = node.variable.token.value
					enterForStatement(node)
				}
				is FunctionCall -> enterFunctionCall(node)
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
				is Block -> leaveBlock()
				is ForStatement -> currentlyDeclaredVariableName = null
			}
			return true
		}
	}

	private fun enterDeclaration(declaration: Declaration) {
		if (declaration.store && !allowStoreDeclaration) {
			throw SemanticError(declaration.location, Translations.getString("base.dsl.unexpectedStore.msg"))
		}
		declareVariableInLocalScope(declaration.left.token.value as String, declaration.location)
	}

	protected open val allowStoreDeclaration: Boolean get() = scope.scopeLevel <= 1

	private fun declareVariableInLocalScope(name: String, location: CodeLocation) {
		val typeSymbol = null as BuiltInTypeSymbol?
		val varSymbol = VariableSymbol(name, typeSymbol)
		if (scope.hasSymbol(name, currentScopeOnly = true)) {
			throw SemanticError(location, Translations.getString("base.dsl.variableAlreadyDeclared.msg", name))
		}
		scope.define(varSymbol)
	}

	private fun enterAssignment(assignment: Assignment) {
		// If implicit declaration wouldn't be supported, we would check here
		// whether the variable is already declared, and if not, throw an error
		val varName = assignment.left.token.value as String
		val typeSymbol = null as BuiltInTypeSymbol?
		if (!scope.hasSymbol(varName)) {
			val varSymbol = VariableSymbol(varName, typeSymbol)
			scope.define(varSymbol)
		}
	}

	private fun enterForStatement(forStatement: ForStatement) {
		declareVariableInLocalScope(forStatement.variable.token.value as String, forStatement.location)
	}

	private fun enterBlock() {
		scope = ScopedSymbolTable("block", scopeLevel = scope.scopeLevel + 1, enclosingScope = scope)
	}

	private fun enterFunctionCall(functionCall: FunctionCall) {
		val name = functionCall.name.value!!
		val functionSymbol = scope.lookup(name)
			?: throw SemanticError(functionCall.location, Translations.getString("base.dsl.functionNotDefined.msg", name))
		if (functionSymbol !is ExternalFunctionSymbol) {
			throw SemanticError(functionCall.location, Translations.getString("base.dsl.expectedFunction.msg", name))
		}
		if (functionCall.params.size != functionSymbol.paramsCount) {
			throw SemanticError(functionCall.location, Translations.getString("base.dsl.functionParamCount.msg", functionSymbol.paramsCount))
		}
		functionCall.function = functionSymbol
	}

	private fun leaveBlock() {
		scope.enclosingScope?.let { scope = it }
	}

	private fun variable(variable: Variable) {
		val name = variable.token.value as String
		if (!scope.hasSymbol(name) && currentlyDeclaredVariableName != name) {
			throw SemanticError(variable.location, Translations.getString("base.dsl.variableNotDefined.msg", name))
		}
	}
}