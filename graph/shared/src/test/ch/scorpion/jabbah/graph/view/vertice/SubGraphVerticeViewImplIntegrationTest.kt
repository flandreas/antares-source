package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.MemoryLibraryPersistenceService
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.io.StorableCloner
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SubGraphVerticeViewImplIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldYieldNonOverwrittenLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl

		vv.label = null

		assertEquals("TEST", vv.label!!.getTranslation())
		assertEquals("TEST", vv.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldOverwriteLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl

		vv.label = TranslatableText("CHANGED")

		assertEquals("CHANGED", vv.label!!.getTranslation())
		assertEquals("CHANGED", vv.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldStoreOverwrittenLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.label = TranslatableText("CHANGED")

		val clone = StorableCloner.clone(vv)

		assertEquals("CHANGED", clone.label!!.getTranslation())
		assertEquals("CHANGED", clone.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldNotStoreUnchangedLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl

		val data = StorableCloner.serialize(vv)

		assertFalse(data.contains("TEST"))
	}

	@Test
	fun shouldResetLabelToDefault() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.label = TranslatableText("")

		val clone = StorableCloner.clone(vv)

		assertEquals("TEST", clone.label!!.getTranslation())
		assertEquals("TEST", clone.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldClearLabelWithBlank() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.label = TranslatableText(" ")

		val clone = StorableCloner.clone(vv)

		assertEquals(" ", clone.label!!.getTranslation())
		assertEquals(" ", clone.getLabelComponent()!!.text.getTranslation())
	}

	@Test
	fun shouldUseExecutionLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.model.bind(true, LibraryModule.libraryHolder.library)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		vv.executionStarted(signalHandler)

		vv.executionLabel = TranslatableText("EXEC")

		assertEquals("EXEC", vv.getLabelComponent()!!.label.text)
	}

	@Test
	fun shouldResetExecutionLabel() {
		val vv = createLibraryElementWithLabel("TEST").getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vv.model.bind(true, LibraryModule.libraryHolder.library)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		vv.executionStarted(signalHandler)
		vv.executionLabel = TranslatableText("EXEC")

		vv.model.executionStopped(signalHandler)
		vv.executionStopped(signalHandler)

		assertEquals("TEST", vv.getLabelComponent()!!.label.text)
	}

	private fun createLibraryElementWithLabel(label: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library, label)
		return library.getContainerLibraryElement(metaGraph.uuid)!!
	}
}