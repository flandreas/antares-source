package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Integration test of a [GraphView] containing a connected [SubGraphVerticeView]
 * in which a referenced [GraphPortView] has been removed in the meantime.
 */
class StaleGraphPortViewTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldLoadMetaGraphWithStaleGraphPortViewReference() {
		val builder = TestLibraryBuilder()
		val inner = builder.addInnerCustomComponent(LibraryModule.libraryHolder.library)
		val outer = builder.addOuterCustomComponent(LibraryModule.libraryHolder.library)
		removeInputPortFromInner(inner)

		val element = ContainerLibraryElement(outer.uuid)
		LibraryModule.libraryService.loadMetaGraph(
			LibraryModule.libraryHolder.library,
			element)

		assertNotNull(element.storable!!.graph.graphView.getGraphPortView("O")!!.model.getPort<Boolean>().net!!.designError)
	}

	private fun removeInputPortFromInner(inner: MetaGraph) {
		// TODO Refactoring: Synchronization of the two sub-models of MetaGraph is currently done in
		// ContainerEditor and ContainerTree. This should be done on the model layer. i.e. in MetaGraph itself

		with(inner.graph.graphView) {
			remove(getGraphPortView("I")!!)
		}
		with(inner.containerDrawing) {
			remove(getPortViewComponent("I")!!)
		}

		LibraryModule.libraryService.updateContainerLibraryElement(
			LibraryModule.libraryHolder.library,
			inner,
			ContainerLibraryElement(inner.uuid))
	}
}