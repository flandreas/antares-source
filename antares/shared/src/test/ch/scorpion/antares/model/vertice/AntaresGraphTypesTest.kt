package ch.scorpion.antares.model.vertice

import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.jabbah.graph.library.BaseLibraryElement
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.GraphType
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AntaresGraphTypesTest {

	@Test
	fun canImportSameBaseLibraryElement() {
		assertTrue(Digital.canImport(baseLibraryElement(Digital)))
		assertTrue(Analog.canImport(baseLibraryElement(Analog)))
	}

	@Test
	fun cannotImportNotSameBaseLibraryElement() {
		assertFalse(Digital.canImport(baseLibraryElement(Analog)))
		assertFalse(Analog.canImport(baseLibraryElement(Digital)))
	}

	@Test
	fun cannotImportNonAntaresBaseLibraryElement() {
		assertFalse(Digital.canImport(baseLibraryElement(GenericGraphType)))
		assertFalse(Analog.canImport(baseLibraryElement(GenericGraphType)))
	}

	@Test
	fun digitalCanImportAnalogContainerLibraryElement() {
		assertTrue(Digital.canImport(containerLibraryElement(Analog)))
	}

	@Test
	fun digitalCanImportDigitalContainerLibraryElements() {
		assertTrue(Digital.canImport(containerLibraryElement(Digital)))
	}

	@Test
	fun analogCannotImportDigitalContainerLibraryElement() {
		assertFalse(Analog.canImport(containerLibraryElement(Digital)))
	}

	@Test
	fun analogCannotImportAnalogContainerLibraryElement() {
		assertFalse(Analog.canImport(containerLibraryElement(Analog)))
	}

	private fun baseLibraryElement(graphType: GraphType): BaseLibraryElement =
		mockk<BaseLibraryElement>().also { every { it.graphType }.returns(graphType) }

	private fun containerLibraryElement(graphType: GraphType): ContainerLibraryElement =
		mockk<ContainerLibraryElement>().also { every { it.graphType }.returns(graphType) }
}