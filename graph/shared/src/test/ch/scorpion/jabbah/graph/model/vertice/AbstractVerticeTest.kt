package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import org.junit.Assert.*
import org.junit.ClassRule
import ch.scorpion.jabbah.graph.model.port.PortImpl
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test


/**
 * Unit tests for [AbstractVertice].
 */
class AbstractVerticeTest {

    private val vertice = MyVertice()

    companion object {
        @ClassRule @JvmField
        val rule = GraphModelTestRule()
    }

    @Test
    fun shouldAccessInputsById() {
        vertice.addPort(PortImpl.createInput(Boolean::class, "A"))
        vertice.addPort(PortImpl.createInput(Boolean::class, "B"))

        assertThat(vertice.getInput<Boolean>(1).name, `is`("A"))
        assertThat(vertice.getInput<Boolean>(2).name, `is`("B"))
    }

    @Test
    fun shouldAccessOutputsById() {
        vertice.addPort(PortImpl.createOutput(Boolean::class, "A"))
        vertice.addPort(PortImpl.createOutput(Boolean::class, "B"))

        assertThat(vertice.getOutput<Boolean>(1).name, `is`("A"))
        assertThat(vertice.getOutput<Boolean>(2).name, `is`("B"))
    }

    private class MyVertice : AbstractVertice("graph.property.label") {
        override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
            return true
        }
    }
}