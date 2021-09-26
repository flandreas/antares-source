package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.dsl.ActivationRecordFactory
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.dsl.GraphDslModule

object AntaresDslModule : AbstractModule() {

	override fun initialize() {
		GraphDslModule.graphViewExternalFunctionsFactory = { AntaresGraphViewExternalFunctions() }
		GraphDslModule.usecaseActionExternalFunctions = AntaresUsecaseActionExternalFunctions
		GraphDslModule.usecaseTestExternalFunctions = AntaresUsecaseTestExternalFunctions

		GraphDslModule.require()

		BaseModule.lexerFactory = { AntaresLexer(it) }
		BaseModule.parserFactory = ParserFactory { p, s -> AntaresParser(BaseModule.lexerFactory(p) as AntaresLexer, s) }
		BaseModule.storingActivationRecordFactory = ActivationRecordFactory { n, p -> AntaresStoringActivationRecord(n, p) }
		BaseModule.interpreterFactory = { n, m -> AntaresInterpreter(n, m) }
	}
}