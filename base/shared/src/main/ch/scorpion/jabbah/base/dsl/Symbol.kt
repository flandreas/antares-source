package ch.scorpion.jabbah.base.dsl

open class Symbol(val name: String, val type: Symbol? = null)

class BuiltInTypeSymbol(name: String) : Symbol(name)

class VariableSymbol(name: String, type: BuiltInTypeSymbol?) : Symbol(name, type)

/**
 * A function implicitly defined by a DSL execution environment and called by an [Interpreter]
 * interpreting a DSL script.
 */
fun interface ExternalFunction {

	/**
	 * The function to be executed.
	 * @param context the optional information about the context in which this [ExternalFunction] is executed.
	 * Implementations will have to cast it to concrete classes known in the corresponding layer
	 * @param params the [List] of function arguments
	 * @return the function result
	 * @throws RuntimeError in case of an error
	 */
	fun execute(params: List<Any>, context: Any?): Any
}

class ExternalFunctionSymbol(
	name: String,
	val paramsCount: Int,
	val function: ExternalFunction
) : Symbol(name)


