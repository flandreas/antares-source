package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

class MetaGraphTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphViewTestRule()
	}

	@Test
	fun shouldUpdateContainerGraphName() {
		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name = "Changed Name"
		assertThat(metaGraph.containerDrawing.model.name, `is`("Changed Name"))
	}
}