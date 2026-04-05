package io.antarescircuit.jabbah.base.dsl

/**
 * Performs semantic analysis on an Abstract Syntax Tree and throws [SemanticError]
 * in cause of semantic errors.
 */
interface SemanticAnalyser {

	/**
	 * Analyses the Abstract Syntax Tree in [program].
	 * @throws SemanticError in case of a semantic error
	 */
	fun analyse(program: Node)
}