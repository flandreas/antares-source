package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.dsl.GraphDslModule

object AntaresDslModule : AbstractModule() {

	override fun initialize() {
		GraphDslModule.graphViewExternalFunctionsFactory = { AntaresGraphViewExternalFunctions() }
		GraphDslModule.usecaseActionExternalFunctions = AntaresUsecaseActionExternalFunctions
		GraphDslModule.usecaseTestExternalFunctions = AntaresUsecaseTestExternalFunctions

		GraphDslModule.require()

		BaseModule.lexerFactory = { AntaresLexer(it) }
		BaseModule.parserFactory = { p, s -> AntaresParser(BaseModule.lexerFactory(p) as AntaresLexer, s) }
		BaseModule.storingActivationRecordFactory = { n, p -> AntaresStoringActivationRecord(n, p) }
		BaseModule.interpreterFactory = { n, m -> AntaresInterpreter(n, m) }
		BaseModule.dslGlobalFunctions = AntaresDslGlobalFunctions()
	}
}