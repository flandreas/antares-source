package io.antarescircuit.antares.model.vertice

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes.Analog
import io.antarescircuit.antares.model.AntaresGraphTypes.Digital
import io.antarescircuit.jabbah.graph.library.BaseLibraryElement
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.GraphType
import kotlin.test.*

class AntaresGraphTypesTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun canImportSameBaseLibraryElement() {
		assertNull(Digital.checkImport(baseLibraryElement(Digital)))
		assertNull(Analog.checkImport(baseLibraryElement(Analog)))
	}

	@Test
	fun cannotImportNotSameBaseLibraryElement() {
		assertNotNull(Digital.checkImport(baseLibraryElement(Analog)))
		assertNotNull(Analog.checkImport(baseLibraryElement(Digital)))
	}

	@Test
	fun cannotImportNonAntaresBaseLibraryElement() {
		assertNotNull(Digital.checkImport(baseLibraryElement(GenericGraphType)))
		assertNotNull(Analog.checkImport(baseLibraryElement(GenericGraphType)))
	}

	@Test
	fun digitalCanImportAnalogContainerLibraryElement() {
		assertNull(Digital.checkImport(containerLibraryElement(Analog)))
	}

	@Test
	fun digitalCanImportDigitalContainerLibraryElements() {
		assertNull(Digital.checkImport(containerLibraryElement(Digital)))
	}

	@Test
	fun analogCannotImportDigitalContainerLibraryElement() {
		assertNotNull(Analog.checkImport(containerLibraryElement(Digital)))
	}

	@Test
	fun analogCannotImportAnalogContainerLibraryElement() {
		assertNotNull(Analog.checkImport(containerLibraryElement(Analog)))
	}

	private fun baseLibraryElement(graphType: GraphType): BaseLibraryElement =
		BaseLibraryElement(graphType)

	private fun containerLibraryElement(graphType: GraphType): ContainerLibraryElement =
		ContainerLibraryElement(graphType = graphType)
}