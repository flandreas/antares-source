package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.dsl.BaseTokenType.ID
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.parser.Token
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphFunctionContext
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/**
 * Unit tests for Antares DSL that incorporate mocked [Port]s.
 */
abstract class AbstractAntaresInterpreterPortTest {

	protected val context = BaseModule.storingActivationRecordFactory("context", null)

	init {
		AntaresTestRule.configure()
	}

	protected fun variable(name: String): Variable = Variable(TextLocation(0, 0, 0), Token(ID, name))

	protected fun setInput(name: String, signal: DigitalSignal, interpreter: AntaresInterpreter) {
		val port = mock<DigitalPort>()
		every { port.name } returns name
		every { port.portId } returns 1

		setInput(createPort(name), signal, interpreter)
	}

	protected fun createPort(name: String, logic: Logic = Logic.POSITIVE): DigitalPort {
		val port = mock<DigitalPort>()
		every { port.name } returns name
		every { port.portId } returns 1
		every { port.logic } returns logic
		return port
	}

	protected fun setInput(port: DigitalPort, signal: DigitalSignal, interpreter: AntaresInterpreter) {
		val data = mock<GraphActorData>()
		every { data.changedPort } returns port
		every { data.getSignal<DigitalSignal>(any()) } returns signal

		context.setValue(variable(port.name!!), signal)
		interpreter.interpret(SubGraphFunctionContext(data, null, null))
	}
}