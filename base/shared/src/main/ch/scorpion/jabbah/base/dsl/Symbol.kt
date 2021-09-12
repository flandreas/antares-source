package ch.scorpion.jabbah.base.dsl

open class Symbol(val name: String, val type: Symbol? = null)

class BuiltInTypeSymbol(name: String) : Symbol(name)

class VariableSymbol(name: String, type: BuiltInTypeSymbol?) : Symbol(name, type)

class ScopedSymbolTable(
	val name: String,
	val level: Int,
	val enclosingScope: ScopedSymbolTable?
) {

	private val symbols = mutableMapOf<String, Symbol>()

	val size: Int get() = symbols.size

	init {
		if (level <= 1) {
			Lexer.getReservedWords().forEach {
				define(BuiltInTypeSymbol(it))
			}
		}
	}

	override fun toString(): String = "Scope $name at level $level"

	fun define(symbol: Symbol) {
		symbols[symbol.name] = symbol
	}

	fun lookup(name: String, currentScopeOnly: Boolean = false): Symbol? {
		if (currentScopeOnly) {
			return symbols[name]
		}
		return symbols[name] ?: enclosingScope?.lookup(name)
	}
}

