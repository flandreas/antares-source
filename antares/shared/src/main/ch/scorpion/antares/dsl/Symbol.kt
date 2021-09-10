package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor

open class Symbol(val name: String, val type: Symbol? = null)

class BuiltInTypeSymbol(name: String) : Symbol(name)

class VariableSymbol(name: String, type: BuiltInTypeSymbol?) : Symbol(name, type)

class SymbolTable {

	private val symbols = mutableMapOf<String, Symbol>()

	val size: Int get() = symbols.size

	init {
		Lexer.getReservedWords().forEach {
			define(BuiltInTypeSymbol(it))
		}
	}

	fun define(symbol: Symbol) {
		symbols[symbol.name] = symbol
	}

	fun lookup(name: String): Symbol? {
		return symbols[name]
	}
}

class SymbolTableBuilder : EmptyHierarchyVisitor() {

	private val symbolTable = SymbolTable()

	private var currentlyDeclaredVariableName: String? = null

	fun build(): SymbolTable = symbolTable

	override fun visitEnter(node: Any): Boolean {
		when (node) {
			is Declaration -> {
				currentlyDeclaredVariableName = node.left.token.value
				visitDeclaration(node)
			}
			is Assignment -> {
				currentlyDeclaredVariableName = node.left.token.value
				visitAssignment(node)
			}
		}
		return true
	}

	override fun visit(node: Any): Boolean {
		when (node) {
			is Variable -> visitVariable(node)
		}
		return true
	}

	override fun visitLeave(node: Any): Boolean {
		when (node) {
			is Declaration -> currentlyDeclaredVariableName = null
			is Assignment -> currentlyDeclaredVariableName = null
		}
		return true
	}

	private fun visitVariable(variable: Variable) {
		val name = variable.token.value as String
		if (symbolTable.lookup(name) == null && currentlyDeclaredVariableName != name) {
			throw SemanticError(variable.location, "Variable '$name' not defined")
		}
	}

	private fun visitDeclaration(declaration: Declaration) {
		val varName = declaration.left.token.value as String
		val typeSymbol = null as BuiltInTypeSymbol?
		val varSymbol = VariableSymbol(varName, typeSymbol)
		symbolTable.define(varSymbol)
	}

	private fun visitAssignment(assignment: Assignment) {
		// If implicit declaration wouldn't be supported, we would check here
		// whether the variable is already declared, and if not, throw an error
		val varName = assignment.left.token.value as String
		val typeSymbol = null as BuiltInTypeSymbol?
		if (symbolTable.lookup(varName) == null) {
			val varSymbol = VariableSymbol(varName, typeSymbol)
			symbolTable.define(varSymbol)
		}
	}
}