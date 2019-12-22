package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals

class MetaGraphTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldUpdateContainerGraphName() {
		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name.value = "Changed Name"
		assertEquals("Changed Name", metaGraph.containerDrawing.model.name)
	}

	@Test
	fun shouldForwardIdInfoToGraphWhenLoading() {
		val uuid = System.createUUID()
		val name = "Some Name"
		val metaGraph = MetaGraph()
		metaGraph.containerDrawing.model.graphUUID = uuid
		metaGraph.containerDrawing.model.name = name

		val clone = StorableCloner.clone(metaGraph) as MetaGraph

		assertEquals(uuid, clone.uuid)
		assertEquals(name, clone.name)
	}
}