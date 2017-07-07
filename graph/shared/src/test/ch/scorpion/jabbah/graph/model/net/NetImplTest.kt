package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.Net
import org.junit.Assert.*
import org.junit.ClassRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import ch.scorpion.jabbah.graph.model.TestVertice

/**
 * Unit tests for [NetImpl].
 */
class NetImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphModelTestRule()
    }

    @Test
    fun shouldConnectPort() {
        val v = TestVertice()
        val net = NetImpl<Boolean>()
        net.connect(v.getOutput<Boolean>())

        assertThat(net.isConnectedWith(v.getOutput<Boolean>()), `is`(true))
        assertThat(net.portsCount, `is`(1))
    }

    @Test
    fun shouldUnconnectPort() {
        val v = TestVertice()
        val net = NetImpl<Boolean>()
        net.connect(v.getOutput<Boolean>())
        net.unconnect(v.getOutput<Boolean>())

        assertThat(net.isConnectedWith(v.getOutput<Boolean>()), `is`(false))
        assertThat(net.portsCount, `is`(0))
    }
}