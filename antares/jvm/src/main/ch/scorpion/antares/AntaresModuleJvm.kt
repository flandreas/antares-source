package ch.scorpion.antares

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.gate.UndefinedGateInputBehaviorPreference
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.*
import ch.scorpion.antares.view.container.DigitalContainerEditor
import ch.scorpion.antares.view.container.DigitalContainerToolBarBuilder
import ch.scorpion.antares.view.container.DigitalContainerTreeView
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.oscilloscope.DigitalSignalHistoryDrawer
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorPreference
import ch.scorpion.antares.view.output.VideoRamColorModel
import ch.scorpion.antares.view.port.DigitalPortViewStyle
import ch.scorpion.antares.view.signal.*
import ch.scorpion.jabbah.app.ApplicationVersionServiceImpl
import ch.scorpion.jabbah.app.RailwayAppUsageServiceImpl
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.DataLocation
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.auth0.Auth0LoginFlow
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.sound.WaveformType
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.AbstractReflectionPropertySwing
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.view.DynamicPropertyRendererRegistry
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamValueEditorRegistry
import ch.scorpion.jabbah.graph.model.param.GraphParamValuePropertyFactory
import ch.scorpion.jabbah.graph.model.param.GraphParamValuePropertyFactoryRegistry
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.ProjectAkrabClientServiceJvm
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap
import java.nio.file.FileSystems

/**
 * Module definitions for the [ch.scorpion.antares] module on the JVM target.
 */
class AntaresModuleJvm(private val app: AntaresDesktop) : AbstractModule() {

	companion object {
		const val PREF_TREE_CIRCUIT = "antares.preferences.group.circuit"
	}

	override fun initialize() {
		GraphViewModule.containerEditorFactory = { DigitalContainerEditor(it) }

		GraphViewModuleJvm.containerToolBarBuilderFactory = { DigitalContainerToolBarBuilder() }
		GraphModuleJvm.containerTreeViewFactory = { DigitalContainerTreeView() }

		GraphModuleJvm.require()
		AntaresViewModule.require()

		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(
			dataPath = app.fileStoreBasePath,
			directoryName = app.userLibraryDirectoryName,
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName)

		LibraryModule.systemLibraryPersistenceService = if (app.systemLibraryBasePath != null) {
			FileLibraryPersistenceService(
				dataPath = app.systemLibraryBasePath!!,
				directoryName = AntaresApplication.DEFAULT_LIB_DIRECTORY,
				metaGraphFileExtension = app.fileExtension,
				libraryFileName = app.libraryFileName)
		} else {
			ResourceLibraryPersistenceService(
				metaGraphFileExtension = app.fileExtension,
				libraryFileName = app.libraryFileName
			)
		}


		LibraryModule.libraryFactory = AntaresLibraryFactory()
		LibraryModule.libraryService = LibraryService()

		LibraryModule.userLibraryDictionaryService = LibraryDictionaryService(
			FileLibraryDictionaryPersistenceService(
				"${app.fileStoreBasePath}${FileSystems.getDefault().separator}${app.userLibraryDirectoryName}"))

		LibraryModule.systemLibraryDictionaryService = if (app.systemLibraryBasePath != null) {
			LibraryDictionaryService(FileLibraryDictionaryPersistenceService(
				"${app.systemLibraryBasePath!!}${FileSystems.getDefault().separator}${app.userLibraryDirectoryName}"))
		} else {
			LibraryDictionaryService(ResourceLibraryDictionaryPersistenceService())
		}

		LibraryModule.libraryManagementService = LibraryManagementService()

		ProjectModule.projectDictionaryService = LibraryDictionaryService(
			(FileLibraryDictionaryPersistenceService(
				"${app.fileStoreBasePath}${FileSystems.getDefault().separator}${app.projectDirectoryName}")))


		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService(
			dataPath = app.fileStoreBasePath,
			directoryName = app.projectDirectoryName,
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName)

		ProjectModule.projectManagementService = { ProjectManagementService(
			newMetaGraphNameTranslationKey = "graph.name.unknown") }

		if (app.dataLocation == DataLocation.Local) {
			GraphModuleJvm.projectAkrabClientServiceJvm = {
				ProjectAkrabClientServiceJvm(
					app.dataUrl!!,
					ProjectModule.projectLibraryPersistenceService as FileLibraryPersistenceService
				)
			}
		}

		customizeProperties(BaseModule.properties)

		configureTypeMap(IOModule.typeMap)
		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)
		configureGraphParamValueProperties()
		configureGraphParamValueEditors()

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	private fun customizeProperties(properties: Properties) {
		properties.set(ApplicationVersionServiceImpl.PROP_VERSION_FILE_URL, "https://www.antarescircuit.io/version.txt")
		properties.set(RailwayAppUsageServiceImpl.PROP_PING_URL, "https://click-metrics.up.railway.app/api/ping")
		properties.set(RailwayAppUsageServiceImpl.PROP_PING_APPLICATION_ID, "498417e8-efd2-4c78-8a11-317037cc9afa")

		properties.set(Auth0LoginFlow.PROP_AUTH0_DOMAIN, "dev-wq7i977v.eu.auth0.com")
		properties.set(Auth0LoginFlow.PROP_AUTH0_CLIENT_ID, "mYdmErbSZxQUtlr9BW2UHUOmxtHN8WNO")
		properties.set(Auth0LoginFlow.PROP_AUTH0_AUDIENCE, "https://antarescircuit.io/api")
		properties.set(Auth0LoginFlow.PROP_AUTH0_REDIRECT_URL, "http://127.0.0.1:8899/desktop")
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
		registry.registerRenderer(BitWidth::class.java, BitWidthRenderer::class.java)
		registry.registerRenderer(DigitalSignalRepresentation::class.java, EnumRenderer::class.java)
		registry.registerRenderer(SevenSegmentDisplayScheme::class.java, EnumRenderer::class.java)
		registry.registerRenderer(OutputAnnotation::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PullDirection::class.java, EnumRenderer::class.java)
		registry.registerRenderer(TransistorType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(TransistorViewSymbol::class.java, EnumRenderer::class.java)
		registry.registerRenderer(JoystickDeflectionEditor::class.java, EnumRenderer::class.java)
		registry.registerRenderer(DigitalPortViewStyle::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PortViewSpacing::class.java, EnumRenderer::class.java)
		registry.registerRenderer(WaveformType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(VideoRamColorModel::class.java, EnumRenderer::class.java)
	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(LightColor::class.java) { LightColorEditor((it as CommandPropertySwing<LightColor>).optional) }
		registry.register(InputCount::class.java) { InputCountEditor((it as CommandPropertySwing<InputCount>).filter) }
		registry.register(InputPortNumber::class.java) { InputPortNumberEditor((it as CommandPropertySwing<InputPortNumber>).filter) }
		registry.registerEditor(Handedness::class.java, HandednessEditor::class.java)
		registry.registerEditor(Logic::class.java, LogicEditor::class.java)
		registry.registerEditor(Trigger::class.java, TriggerEditor::class.java)
		registry.register(BranchCount::class.java) { BranchCountEditor((it as CommandPropertySwing<BranchCount>).filter) }
		registry.registerEditor(DigitalSignalRepresentation::class.java, DigitalSignalRepresentationEditor::class.java)
		registry.registerEditor(SevenSegmentDisplayScheme::class.java, SevenSegmentDisplaySchemeEditor::class.java)
		registry.registerEditor(OutputAnnotation::class.java, OutputAnnotationEditor::class.java)
		registry.registerEditor(PullDirection::class.java, PullDirectionEditor::class.java)
		registry.registerEditor(TransistorType::class.java, TransistorTypeEditor::class.java)
		registry.registerEditor(TransistorViewSymbol::class.java, TransistorSymbolEditor::class.java)
		registry.registerEditor(JoystickDeflectionEditor::class.java, JoystickDeflectionEditor::class.java)
		registry.registerEditor(DigitalPortViewStyle::class.java, DigitalPortViewStyleEditor::class.java)
		registry.registerEditor(PortViewSpacing::class.java, PortViewSpacingEditor::class.java)
		registry.registerEditor(WaveformType::class.java, WaveformTypeEditor::class.java)
		registry.registerEditor(VideoRamColorModel::class.java, VideoRamColorModelEditor::class.java)

		registry.register(BitWidth::class.java) { prop ->
			BitWidthEditor(
				propertyName = prop.displayName,
				editable = (prop as BitWidthPropertySwing).editable,
				graphEditor = prop.editor,
				errorCallback = { prop.dslError = it },
				prop.filter )
		}
	}

	private fun configureGraphParamValueProperties() {
		GraphParamValuePropertyFactoryRegistry.register(
			BitWidthGraphParamType,
			object : GraphParamValuePropertyFactory {
				override fun create(
					def: GraphParamDefinition<*>,
					editor: Editor,
					beanProvider: BeanProvider
				): AbstractReflectionPropertySwing<*> {
					return BitWidthParamValuePropertySwing(
						paramDefinition = def as GraphParamDefinition<BitWidth>,
						propertyName = "BitWidth", // only used for logging
						baseKey ="element.property.bitWidth",
						beanProvider,
					)
				}
			}
		)
	}

	private fun configureGraphParamValueEditors() {
		GraphParamValueEditorRegistry.register(BitWidthGraphParamType) { BitWidthGraphParamValueEditor() }
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.getGroup(DrawModuleJvm.PREF_TREE_RENDERING).add(BooleanPreference(
			id = Look.PROP_FILL_BASIC_COMPONENTS,
			nameKey = "antares.preference.fillBasicComponents"
		))

		root.add(PreferenceGroup(PREF_TREE_CIRCUIT))

		root.getGroup(PREF_TREE_CIRCUIT).add(SymbolStylePreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(UndefinedGateInputBehaviorPreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(BooleanPreference(
			id = AndGateView.PROP_DATA_FLOW_ENABLED,
			nameKey = "antares.preferences.AndGateDataFlow"
		))

		root.getGroup(PREF_TREE_CIRCUIT).add(LightColorPreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(DigitalSignalNotationPreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(TunnelViewFacePreference())

		root.getGroup(PREF_TREE_CIRCUIT).add(TransistorSymbolPreference())
		root.getGroup(PREF_TREE_CIRCUIT).add(BooleanPreference(
			id = TransistorView.PROP_TRANSISTOR_CIRCLE,
			nameKey = "antares.preference.TransistorCircle"
		))

		root.getGroup(PREF_TREE_CIRCUIT).add(BooleanPreference(
			id = DigitalEdgeView.PROP_WIDE_BUS_STROKE,
			nameKey = "antares.preference.wideBusStroke"
		))

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