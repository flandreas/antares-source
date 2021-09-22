package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.Token
import ch.scorpion.jabbah.base.dsl.TokenType
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import io.mockk.every
import io.mockk.mockk

/**
 * Unit tests for Antares DSL that incorporate mocked [Port]s.
 */
abstract class AbstractAntaresInterpreterPortTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	protected val context = BaseModule.storingActivationRecordFactory.create("context", null)

	protected fun variable(name: String): Variable = Variable(CodeLocation(0, 0, 0), Token(TokenType.ID, name))

	protected fun setInput(name: String, signal: DigitalSignal, interpreter: AntaresInterpreter) {
		val port = mockk<DigitalPort>()
		every { port.name } returns name
		every { port.portId } returns 1

		setInput(createPort(name), signal, interpreter)
	}

	protected fun createPort(name: String, logic: Logic = Logic.POSITIVE): DigitalPort {
		val port = mockk<DigitalPort>()
		every { port.name } returns name
		every { port.portId } returns 1
		every { port.logic } returns logic
		return port
	}

	protected fun setInput(port: DigitalPort, signal: DigitalSignal, interpreter: AntaresInterpreter) {
		val data = mockk<GraphActorData>()
		every { data.changedPort } returns port
		every { data.getSignal<DigitalSignal>(any()) } returns signal

		context.setValue(variable(port.name!!), signal)
		interpreter.interpret(data)
	}
}