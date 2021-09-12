package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.Parser
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser

/**
 * Extends the grammar in [Parser] by the following productions.
 *
 * <pre>
 *     number : LONG | hexLiteral
 *     hexLiteral : "0x" INTEGER
 * </pre>
 */
class AntaresParser(
	lexer: AntaresLexer,
	semanticAnalyser: HierarchyVisitor = SemanticAnalyser()
) : Parser(lexer, semanticAnalyser) {

	constructor(text: String): this(AntaresLexer(text))
}