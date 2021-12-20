package ch.scorpion.jabbah.base.dsl

open class Symbol(val name: String, val type: Symbol? = null)

class BuiltInTypeSymbol(name: String) : Symbol(name)

class VariableSymbol(name: String, type: BuiltInTypeSymbol?) : Symbol(name, type)

fun interface ExternalFunction {
	fun execute(params: List<Any>): Any
}

class ExternalFunctionSymbol(
	name: String,
	val paramsCount: Int,
	val function: ExternalFunction
) : Symbol(name)


