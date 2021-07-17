package ch.scorpion.antares.module

import ch.scorpion.antares.AntaresApplication
import ch.scorpion.antares.property.*
import ch.scorpion.antares.ui.registerAntaresIconsInProvider
import ch.scorpion.antares.view.AntaresLibraryFactory
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.net.DigitalEdgeView
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.TranslationServiceImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.edit.properties.PropertyPageRendererRegistry
import ch.scorpion.jabbah.graph.library.LibraryManagementService
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.RestLibraryPersistenceService
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.property.EdgeViewPropertyPage
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJs

/**
 * Module definition of the [ch.scorpion.antares] module for the JS platform.
 */
object AntaresModuleJs : AbstractModule() {

	override fun initialize() {
		EditModuleJs.require()

		BaseModuleJs.translationService = TranslationServiceImpl(
			baseUrl = ".."
		)

		GraphViewModuleJs.require()
		AntaresViewModule.require()

		LibraryModule.systemLibraryPersistenceService = RestLibraryPersistenceService(
			baseUrl = "..",
			libraryDirectoryName = AntaresApplication.DEFAULT_LIB_DIRECTORY,
			libraryFileName = AntaresApplication.DEFAULT_LIB_FILENAME
		)
		LibraryModule.libraryFactory = AntaresLibraryFactory()
		LibraryModule.libraryService = LibraryService()

		LibraryModule.libraryManagementService = LibraryManagementService()

		ProjectModule.projectLibraryPersistenceService = RestLibraryPersistenceService(
			baseUrl = "..",
			//libraryDirectoryName = AntaresApplication.DEFAULT_PROJECT_DIRECTORY,
			libraryDirectoryName = "web-projects",
			libraryFileName = AntaresApplication.DEFAULT_LIB_FILENAME
		)

		ProjectModule.projectManagementService = ProjectManagementService(
			newMetaGraphNameTranslationKey = "graph.name.unknown")


		registerAntaresIconsInProvider()

		loadTranslations()

		registerPropertyPageRenderers(EditModuleJs.propertyPageRendererRegistry)
	}

	/** TODO Work around for missing implementation of [Translations] mechanism for texts used for demo application.*/
	private fun loadTranslations() {
		Translations.addKey("execution.action.execute.name", "Simulate")
		Translations.addKey("execution.action.start.desc", "Start simulation")
		Translations.addKey("execution.action.stop.desc", "Stop simulation")
		Translations.addKey("execution.action.speed.name", "Simulation Speed")
		Translations.addKey("execution.systemSpeedCategory.use", "Use")
		Translations.addKey("execution.systemSpeedCategory.observe", "Observe")
		Translations.addKey("execution.systemSpeedCategory.explore", "Explore")
		Translations.addKey("graph.desktop.name", "Desktop")
		Translations.addKey("library.savable.prefix", "Library Element")
		Translations.addKey("library.library.name", "Library")
		Translations.addKey("project.project.name", "Project")
	}

	private fun registerPropertyPageRenderers(registry: PropertyPageRendererRegistry) {
		registry.register(AndGateView::class, AndGateViewPropertyPage())
		registry.register(BufferGateView::class, BufferGateViewPropertyPage())
		registry.register(DelayGateView::class, DelayGateViewPropertyPage())
		registry.register(NandGateView::class, LogicGateViewPropertyPage<NandGateView>())
		registry.register(NorGateView::class, LogicGateViewPropertyPage<NorGateView>())
		registry.register(NotGateView::class, NotGateViewPropertyPage())
		registry.register(OrGateView::class, LogicGateViewPropertyPage<OrGateView>())
		registry.register(XnorGateView::class, LogicGateViewPropertyPage<XnorGateView>())
		registry.register(XorGateView::class, LogicGateViewPropertyPage<XorGateView>())
		registry.register(TriStateBufferGateView::class, TriStateBufferGateViewPropertyPage())
		registry.register(CircuitInOutView::class, CircuitInOutViewPropertyPage())
		registry.register(DigitalEdgeView::class, EdgeViewPropertyPage())
	}
}