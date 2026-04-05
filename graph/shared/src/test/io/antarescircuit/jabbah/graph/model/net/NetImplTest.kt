package io.antarescircuit.jabbah.graph.model.net

import io.antarescircuit.jabbah.graph.model.GraphModelTestRule
import io.antarescircuit.jabbah.graph.model.TestVertice
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetImplTest {

    @BeforeTest
    fun setup() {
        GraphModelTestRule.configure()
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