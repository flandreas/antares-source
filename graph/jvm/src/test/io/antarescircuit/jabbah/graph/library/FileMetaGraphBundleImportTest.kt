package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.MetaGraphBundle
import io.antarescircuit.jabbah.graph.TempFileLibraryTestRule
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.project.ProjectImpl
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.*

class FileMetaGraphBundleImportTest {

	private lateinit var systemLibrary: Library
	private lateinit var project: ProjectImpl

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		TempFileLibraryTestRule.configure()
		systemLibrary = LibraryImpl("System Library").also {
			it.isSystem = true
			LibraryModule.systemLibraryDictionaryService.add(it)
			it.libraryService.storeLibrary(it)
		}
		project = ProjectImpl(name = TranslatableText("Project"))
		project.addImport(systemLibrary.uuid)
		LibraryModule.libraryHolder.l = project
	}

	@Test
	fun shouldRequireCopyConfirmationEvenWhenReplacementWasApproved() {
		val selected = TestLibraryBuilder().addInnerCustomComponent(systemLibrary)
		val originalXml = systemGraphXml(selected)
		val path = writeBundle(MetaGraphBundle().add(selected))

		val result = import(path, replace = true)

		assertEquals(MetaGraphBundleImportResultType.SystemUuidAlreadyExists, result.type)
		assertTrue(project.isEmpty())
		assertEquals(originalXml, systemGraphXml(selected))
		assertTrue(systemLibrary.containsMetaGraphLocally(selected.uuid))
	}

	@Test
	fun shouldCopySystemGraphAndKeepReferencesToSystemSubGraphs() {
		val builder = TestLibraryBuilder()
		val inner = builder.addInnerCustomComponent(systemLibrary)
		val selected = builder.addOuterCustomComponent(systemLibrary)
		val originalXml = systemGraphXml(selected)
		val path = writeBundle(project.createBundle(selected))

		assertEquals(MetaGraphBundleImportResultType.SystemUuidAlreadyExists, import(path).type)
		assertEquals(MetaGraphBundleImportResultType.Success, import(path, copySystem = true).type)

		val copied = project.getMetaGraph(project.metaGraphIds.single())
		assertNotEquals(selected.uuid, copied.uuid)
		assertEquals(selected.name, copied.name)
		assertEquals(copied.uuid, copied.containerDrawing.model.graphUUID)
		assertEquals(inner.uuid, subGraphRef(copied).graphUUID)
		assertTrue(project.getContainingLibrary(selected.uuid)!!.isSystem)
		assertEquals(originalXml, systemGraphXml(selected))

		val restoredProject = project.libraryService.loadLibrary(project.identification, isSystem = false)
		val restoredCopy = restoredProject.getMetaGraph(copied.uuid)
		assertEquals(copied.uuid, restoredCopy.containerDrawing.model.graphUUID)
		assertEquals(inner.uuid, subGraphRef(restoredCopy).graphUUID)
	}

	@Test
	fun shouldRedirectBundleReferencesToCopiedSystemGraphs() {
		val builder = TestLibraryBuilder()
		val inner = builder.addInnerCustomComponent(systemLibrary)
		val outer = builder.addOuterCustomComponent(systemLibrary)
		val innerXml = systemGraphXml(inner)
		val outerXml = systemGraphXml(outer)
		val path = writeBundle(MetaGraphBundle().add(inner).add(outer))

		assertEquals(MetaGraphBundleImportResultType.Success, import(path, copySystem = true).type)

		val copies = project.metaGraphIds.map { project.getMetaGraph(it) }
		val copiedInner = copies.single { it.name == inner.name }
		val copiedOuter = copies.single { it.name == outer.name }
		assertNotEquals(inner.uuid, copiedInner.uuid)
		assertNotEquals(outer.uuid, copiedOuter.uuid)
		assertEquals(copiedInner.uuid, subGraphRef(copiedOuter).graphUUID)
		assertEquals(copiedInner.uuid, subGraphRef(copiedOuter).getGraph().uuid)
		assertEquals(innerXml, systemGraphXml(inner))
		assertEquals(outerXml, systemGraphXml(outer))
	}

	@Test
	fun shouldStillRequireReplacementConfirmationForCustomConflicts() {
		val selected = TestLibraryBuilder(libraryService = project.libraryService).addInnerCustomComponent(project)
		val path = writeBundle(project.createBundle(selected))

		assertEquals(MetaGraphBundleImportResultType.UuidAlreadyExists, import(path).type)
		assertEquals(MetaGraphBundleImportResultType.UuidAlreadyExists, import(path, copySystem = true).type)
		assertEquals(MetaGraphBundleImportResultType.Success, import(path, replace = true).type)
		assertEquals(listOf(selected.uuid), project.metaGraphIds)
		assertEquals(selected.name, project.getMetaGraph(selected.uuid).name)
	}

	@Test
	fun shouldRequireBothConfirmationsForMixedConflicts() {
		val inner = TestLibraryBuilder().addInnerCustomComponent(systemLibrary)
		val outer = TestLibraryBuilder(libraryService = project.libraryService)
			.addOuterCustomComponent(project, innerLibrary = systemLibrary)
		val originalXml = systemGraphXml(inner)
		val path = writeBundle(MetaGraphBundle().add(inner).add(outer))

		assertEquals(MetaGraphBundleImportResultType.SystemUuidAlreadyExists, import(path).type)
		assertEquals(MetaGraphBundleImportResultType.UuidAlreadyExists, import(path, copySystem = true).type)
		assertEquals(listOf(outer.uuid), project.metaGraphIds)
		assertEquals(inner.uuid, subGraphRef(project.getMetaGraph(outer.uuid)).graphUUID)

		assertEquals(MetaGraphBundleImportResultType.Success, import(path, replace = true, copySystem = true).type)

		val copiedInnerId = project.metaGraphIds.single { it != outer.uuid }
		assertNotEquals(inner.uuid, copiedInnerId)
		assertEquals(copiedInnerId, subGraphRef(project.getMetaGraph(outer.uuid)).graphUUID)
		assertEquals(originalXml, systemGraphXml(inner))
	}

	@Test
	fun shouldKeepIdentityWhenThereIsNoConflict() {
		val selected = MetaGraph()
		val path = writeBundle(MetaGraphBundle().add(selected))

		assertEquals(MetaGraphBundleImportResultType.Success, import(path).type)
		assertEquals(listOf(selected.uuid), project.metaGraphIds)
	}

	private fun import(path: String, replace: Boolean = false, copySystem: Boolean = false): MetaGraphBundleImportResult =
		project.libraryService.importMetaGraphBundle(
			path, "Test", project, replace, LibraryModule.libraryHolder, copyIfSystemUuidExists = copySystem,
		)

	private fun writeBundle(bundle: MetaGraphBundle): String {
		// Refresh the imported library after building the source circuits on disk.
		project.addImport(systemLibrary.uuid)
		val path = Files.createTempFile("bundle-import", ".zip").absolutePathString()
		LibraryModule.userLibraryPersistenceService.exportMetaGraphBundle(bundle, path)
		return path
	}

	private fun systemGraphXml(metaGraph: MetaGraph): String =
		LibraryModule.systemLibraryPersistenceService.loadMetaGraphXML(systemLibrary, metaGraph.uuid)

	private fun subGraphRef(metaGraph: MetaGraph): SubGraphVerticeRef =
		metaGraph.graph.model!!.elements.filterIsInstance<SubGraphVerticeRef>().single()
}
