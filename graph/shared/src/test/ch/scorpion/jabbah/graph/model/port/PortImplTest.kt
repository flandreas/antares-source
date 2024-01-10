package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.graph.model.TestVertice
import kotlin.test.Test

class PortImplTest {

	/** Regression test for GitHub issue #685. */
	@Test
	fun shouldClearPortNameFromUI() {
		val vertice = TestVertice()
		vertice.getPort<Boolean>(1).name = "A"
		vertice.getPort<Boolean>(2).name = "B"

		// Property editor produces empty strings rather than null when clearing a field
		vertice.getPort<Boolean>(1).name = ""
		vertice.getPort<Boolean>(2).name = ""
	}
}