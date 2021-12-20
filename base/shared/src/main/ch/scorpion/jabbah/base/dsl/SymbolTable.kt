package ch.scorpion.jabbah.base.dsl

interface SymbolTable {
	val scopeLevel: Int
	val enclosingScope: SymbolTable?
	val symbolsCount: Int
	fun define(symbol: Symbol)
	fun hasSymbol(name: String, currentScopeOnly: Boolean = false): Boolean
	fun lookup(name: String, currentScopeOnly: Boolean = false): Symbol?
}

class ScopedSymbolTable(
	val name: String,
	override val scopeLevel: Int,
	override val enclosingScope: SymbolTable?
) : SymbolTable {

	private val symbols = mutableMapOf<String, Symbol>()

	init {
		if (scopeLevel <= 1) {
			Lexer.getReservedWords().forEach {
				define(BuiltInTypeSymbol(it))
			}
			DslGlobalFunctions.defineIn(this)
		}
	}

	override val symbolsCount: Int get() = symbols.size

	override fun toString(): String = "Scope $name at level $scopeLevel"

	override fun define(symbol: Symbol) {
		symbols[symbol.name] = symbol
	}

	override fun hasSymbol(name: String, currentScopeOnly: Boolean): Boolean {
		if (currentScopeOnly) {
			return symbols.containsKey(name)
		}
		return symbols.containsKey(name) || enclosingScope?.hasSymbol(name) == true
	}

	override fun lookup(name: String, currentScopeOnly: Boolean): Symbol? {
		if (currentScopeOnly) {
			return symbols[name]
		}
		return symbols[name] ?: enclosingScope?.lookup(name)
	}
}