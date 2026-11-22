package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.MetaGraphBundle
import io.antarescircuit.jabbah.graph.TempFileLibraryTestRule
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.project.ProjectImpl
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileMetaGraphBundleExportTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		TempFileLibraryTestRule.configure()
	}

	@Test
	fun shouldExportSelectedSystemLibraryGraphWithoutSubGraphs() {
		val library = createSystemLibrary()
		LibraryModule.libraryHolder.l = library
		val selected = TestLibraryBuilder().addInnerCustomComponent(library)

		val bundle = exportAndRead(selected, library)

		assertEquals(listOf(selected.uuid), bundle.metaGraphs.map { it.uuid })
		assertTrue(bundle.referencedSystemLibraryIds.isEmpty())
	}

	@Test
	fun shouldExportSelectedSystemLibraryGraphFromCurrentProject() {
		val library = createSystemLibrary()
		val project = ProjectImpl(name = TranslatableText("Project"))
		LibraryModule.libraryHolder.l = project
		val selected = TestLibraryBuilder().addInnerCustomComponent(library)
		project.addImport(library.uuid)

		val bundle = exportAndRead(selected, library)

		assertEquals(listOf(selected.uuid), bundle.metaGraphs.map { it.uuid })
		assertTrue(bundle.referencedSystemLibraryIds.isEmpty())
	}

	@Test
	fun shouldExportSelectedSystemLibraryGraphAndReferenceItsSystemSubGraphs() {
		val library = createSystemLibrary()
		LibraryModule.libraryHolder.l = library
		val builder = TestLibraryBuilder()
		builder.addInnerCustomComponent(library)
		val selected = builder.addOuterCustomComponent(library)

		val bundle = exportAndRead(selected, library)

		assertEquals(listOf(selected.uuid), bundle.metaGraphs.map { it.uuid })
		assertEquals(setOf(library.uuid), bundle.referencedSystemLibraryIds)
	}

	@Test
	fun shouldExportProjectGraphWithoutSubGraphs() {
		val project = ProjectImpl(name = TranslatableText("Project"))
		LibraryModule.libraryHolder.l = project
		val selected = TestLibraryBuilder(libraryService = project.libraryService).addInnerCustomComponent(project)

		val bundle = exportAndRead(selected, project)

		assertEquals(listOf(selected.uuid), bundle.metaGraphs.map { it.uuid })
		assertTrue(bundle.referencedSystemLibraryIds.isEmpty())
	}

	@Test
	fun shouldExportProjectSubGraphsBeforeSelectedGraph() {
		val project = ProjectImpl(name = TranslatableText("Project"))
		LibraryModule.libraryHolder.l = project
		val builder = TestLibraryBuilder(libraryService = project.libraryService)
		val inner = builder.addInnerCustomComponent(project)
		val selected = builder.addOuterCustomComponent(project)

		val bundle = exportAndRead(selected, project)

		assertEquals(listOf(inner.uuid, selected.uuid), bundle.metaGraphs.map { it.uuid })
		assertTrue(bundle.referencedSystemLibraryIds.isEmpty())
	}

	@Test
	fun shouldReferenceSystemSubGraphsWhenExportingProjectGraph() {
		val library = createSystemLibrary()
		val project = ProjectImpl(name = TranslatableText("Project"))
		LibraryModule.libraryHolder.l = project
		TestLibraryBuilder().addInnerCustomComponent(library)
		project.addImport(library.uuid)
		val selected = TestLibraryBuilder(libraryService = project.libraryService)
			.addOuterCustomComponent(project, innerLibrary = library)

		val bundle = exportAndRead(selected, project)

		assertEquals(listOf(selected.uuid), bundle.metaGraphs.map { it.uuid })
		assertEquals(setOf(library.uuid), bundle.referencedSystemLibraryIds)
	}

	private fun createSystemLibrary(): Library = LibraryImpl("System Library").also {
		it.isSystem = true
		LibraryModule.systemLibraryDictionaryService.add(it)
	}

	private fun exportAndRead(selected: MetaGraph, library: Library): MetaGraphBundle {
		val path = Files.createTempFile("bundle-export", ".zip").absolutePathString()
		library.libraryService.exportMetaGraphBundle(
			library.getContainerLibraryElement(selected.uuid)!!,
			LibraryModule.libraryHolder,
			path,
		)
		return LibraryModule.userLibraryPersistenceService.importMetaGraphBundle(path)
	}
}
