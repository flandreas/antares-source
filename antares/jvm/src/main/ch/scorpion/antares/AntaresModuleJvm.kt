package ch.scorpion.antares

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.*
import ch.scorpion.antares.view.container.DigitalContainerEditor
import ch.scorpion.antares.view.container.DigitalContainerTreeView
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.net.TunnelViewFacePreference
import ch.scorpion.antares.view.oscilloscope.DigitalSignalHistoryDrawer
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorPreference
import ch.scorpion.antares.view.signal.DigitalSignalNotationPreference
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.edit.view.DynamicPropertyRendererRegistry
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares] module on the JVM target.
 */
class AntaresModuleJvm(private val app: Antares) : AbstractModule() {

	companion object {
		const val PREF_TREE_CIRCUIT = "antares.preferences.group.circuit"
	}

	override fun initialize() {

		GraphViewModule.containerEditorFactory = { createContainerEditor(it) }
		GraphModuleJvm.containerTreeViewFactory = { DigitalContainerTreeView() }

		GraphModuleJvm.require()
		AntaresViewModule.require()

		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(
			directoryPath = app.userLibraryDirectoryPath,
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName
		)
		LibraryModule.systemLibraryPersisterService = if (app.systemLibraryDirectoryPath != null) {
			FileLibraryPersistenceService(
				directoryPath = app.systemLibraryDirectoryPath!!,
				metaGraphFileExtension = app.fileExtension,
				libraryFileName = app.libraryFileName
			)
		} else {
			ResourceLibraryPersistenceService(
				metaGraphFileExtension = app.fileExtension,
				libraryFileName = app.libraryFileName
			)
		}


		LibraryModule.libraryFactory = AntaresLibraryFactory()
		LibraryModule.libraryService = LibraryService()

		LibraryModule.userLibraryDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService(
			app.userLibraryDirectoryPath))

		LibraryModule.systemLibraryDictionaryService = if (app.systemLibraryDirectoryPath != null) {
			LibraryDictionaryService(FileLibraryDictionaryPersistenceService(app.systemLibraryDirectoryPath!!))
		} else {
			LibraryDictionaryService(ResourceLibraryDictionaryPersistenceService())
		}

		LibraryModule.libraryManagementService = LibraryManagementService()

		ProjectModule.projectDictionaryService = LibraryDictionaryService((FileLibraryDictionaryPersistenceService(
			app.projectsDirectoryPath)))

		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService(
			directoryPath = app.projectsDirectoryPath,
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName
		)

		ProjectModule.projectManagementService = ProjectManagementService(
			newMetaGraphNameTranslationKey = "graph.name.unknown")

		configureTypeMap(IOModule.typeMap)
		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	private fun createContainerEditor(eventBus: EventBus): ContainerEditor {
		val containerCanvas = CanvasJvm { EditModule.drawingViewFactory.invoke(ContainerDrawing(), it) }
		return DigitalContainerEditor(containerCanvas.view as DrawingView<Drawing<Component>>, eventBus)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("text", TextComponentJvm::class)
	}

	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.registerRenderer(LightColor::class.java, LightColorRenderer::class.java)
		registry.registerRenderer(InputCount::class.java, EnumRenderer::class.java)
		registry.registerRenderer(InputPortNumber::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Handedness::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Logic::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Trigger::class.java, EnumRenderer::class.java)
		registry.registerRenderer(BranchCount::class.java, EnumRenderer::class.java)
		registry.registerRenderer(BitWidth::class.java, EnumRenderer::class.java)
		registry.registerRenderer(DigitalSignalRepresentation::class.java, EnumRenderer::class.java)
		registry.registerRenderer(SevenSegmentDisplayScheme::class.java, EnumRenderer::class.java)
		registry.registerRenderer(OutputAnnotation::class.java, EnumRenderer::class.java)
	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(LightColor::class.java) { LightColorEditor((it as PropertyImpl<LightColor>).optional) }
		registry.register(InputCount::class.java) { InputCountEditor((it as PropertyImpl<InputCount>).filter) }
		registry.register(InputPortNumber::class.java) { InputPortNumberEditor((it as PropertyImpl<InputPortNumber>).filter) }
		registry.registerEditor(Handedness::class.java, HandednessEditor::class.java)
		registry.registerEditor(Logic::class.java, LogicEditor::class.java)
		registry.registerEditor(Trigger::class.java, TriggerEditor::class.java)
		registry.registerEditor(BitWidth::class.java, BitWidthEditor::class.java)
		registry.register(BranchCount::class.java) { BranchCountEditor((it as PropertyImpl<BranchCount>).filter) }
		registry.registerEditor(DigitalSignalRepresentation::class.java, DigitalSignalRepresentationEditor::class.java)
		registry.registerEditor(SevenSegmentDisplayScheme::class.java, SevenSegmentDisplaySchemeEditor::class.java)
		registry.registerEditor(OutputAnnotation::class.java, OutputAnnotationEditor::class.java)
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.getGroup(DrawModuleJvm.PREF_TREE_RENDERING).add(BooleanPreference(
			id = Look.PROP_FILL_BASIC_COMPONENTS,
			nameKey = "antares.preference.fillBasicComponents"
		))

		root.add(PreferenceGroup(PREF_TREE_CIRCUIT))

		root.getGroup(PREF_TREE_CIRCUIT).add(BooleanPreference(
			id = AndGateView.PROP_DATA_FLOW_ENABLED,
			nameKey = "antares.preferences.AndGateDataFlow"
		))

		root.getGroup(PREF_TREE_CIRCUIT).add(LightColorPreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(DigitalSignalNotationPreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(TunnelViewFacePreference())

		root.getGroup(PREF_TREE_CIRCUIT).add(IntPreference(
			id = Switch.PROP_DEFAULT_DELAY,
			nameKey = "antares.preference.SwitchPropDelay"
		))

		root.getGroup(GraphViewModuleJvm.PREF_TREE_OSCILLOSCOPE).add(BooleanPreference(
			id = DigitalSignalHistoryDrawer.PROP_FILL_SIGNAL,
			nameKey = "antares.preference.DigitalSignalHistory.fill"
		))
	}
}