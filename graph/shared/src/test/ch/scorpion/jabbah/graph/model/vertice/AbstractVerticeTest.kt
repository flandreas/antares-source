package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.port.PortImpl
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [AbstractVertice].
 */
class AbstractVerticeTest {

    private val vertice = MyVertice()

    companion object {
	    init {
	    	GraphModelTestRule.configure()
	    }
    }

    @Test
    fun shouldAccessInputsById() {
        vertice.addPort(PortImpl.createInput(Boolean::class, "A"))
        vertice.addPort(PortImpl.createInput(Boolean::class, "B"))

        assertEquals("A", vertice.getInput<Boolean>(1).name)
        assertEquals("B", vertice.getInput<Boolean>(2).name)
    }

    @Test
    fun shouldAccessOutputsById() {
        vertice.addPort(PortImpl.createOutput(Boolean::class, "A"))
        vertice.addPort(PortImpl.createOutput(Boolean::class, "B"))

        assertEquals("A", vertice.getOutput<Boolean>(1).name)
        assertEquals("B", vertice.getOutput<Boolean>(2).name)
    }

    private class MyVertice : AbstractVertice("graph.property.label") {
        override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
            return true
        }
    }
}