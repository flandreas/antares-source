package ch.scorpion.antares.model.vertice

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.jabbah.graph.library.BaseLibraryElement
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.GraphType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.*

class AntaresGraphTypesTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
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