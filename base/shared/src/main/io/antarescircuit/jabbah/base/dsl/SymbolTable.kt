package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.module.BaseModule

interface SymbolTable {
	val scopeLevel: Int
	val enclosingScope: SymbolTable?
	val symbolsCount: Int

	fun names(): Iterator<String>
	fun define(symbol: Symbol)
	fun hasSymbol(name: String, currentScopeOnly: Boolean = false): Boolean
	fun lookup(name: String, currentScopeOnly: Boolean = false): Symbol?
	fun canWrite(name: String): Boolean
}

class ScopedSymbolTable(
	val name: String,
	override val scopeLevel: Int,
	override val enclosingScope: SymbolTable?
) : SymbolTable {

	private val symbols = mutableMapOf<String, Symbol>()

	init {
		if (scopeLevel <= 1) {
			DslLexer.getReservedWords().forEach {
				define(BuiltInTypeSymbol(it))
			}
			BaseModule.dslGlobalFunctions.defineIn(this)
		}
	}

	override val symbolsCount: Int get() = symbols.size

	override fun names(): Iterator<String> = symbols.keys.iterator()

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

	override fun canWrite(name: String): Boolean {
		if (hasSymbol(name, true)) {
			return true
		}
		return enclosingScope?.canWrite(name) ?: false
	}
}