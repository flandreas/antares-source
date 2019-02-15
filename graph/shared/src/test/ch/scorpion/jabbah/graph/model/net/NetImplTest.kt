package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.TestVertice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [NetImpl].
 */
class NetImplTest {

    companion object {
	    init {
		    GraphModelTestRule.configure()
	    }
    }

    @Test
    fun shouldConnectPort() {
        val v = TestVertice()
        val net = NetImpl<Boolean>()
        net.connect(v.getOutput())

        assertTrue(net.isConnectedWith(v.getOutput()))
        assertEquals(1, net.portsCount)
    }

    @Test
    fun shouldUnconnectPort() {
        val v = TestVertice()
        val net = NetImpl<Boolean>()
        net.connect(v.getOutput())
        net.unconnect(v.getOutput<Boolean>())

        assertFalse(net.isConnectedWith(v.getOutput()))
        assertEquals(0, net.portsCount)
    }
}