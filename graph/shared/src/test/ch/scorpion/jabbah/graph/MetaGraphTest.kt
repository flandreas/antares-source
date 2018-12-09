package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.io.StorableCloner
import ch.scorpion.jabbah.io.StorableClonerJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test
import java.util.*

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

	@Test
	fun shouldForwardIdInfoToGraphWhenLoading() {
		val uuid = System.get().createUUID()
		val name = "Some Name"
		val metaGraph = MetaGraph()
		metaGraph.containerDrawing.model.graphUUID = uuid
		metaGraph.containerDrawing.model.name = name

		val clone = StorableClonerJvm().clone(metaGraph) as MetaGraph

		assertThat(clone.uuid, `is`(uuid))
		assertThat(clone.name, `is`(name))
	}
}