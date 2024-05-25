package ch.scorpion.antares

import ch.scorpion.antares.hdl.vhdl.ExportVHDLPanel
import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.gate.UndefinedGateInputBehavior
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.testcase.TestcaseViewSwing
import ch.scorpion.antares.view.*
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.container.DigitalContainerEditor
import ch.scorpion.antares.view.container.DigitalContainerToolBarBuilder
import ch.scorpion.antares.view.container.DigitalContainerTreeView
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.graph.AnalogMetaGraphIcon
import ch.scorpion.antares.view.graph.AntaresMetaGraphIcon
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorPreference
import ch.scorpion.antares.view.output.VideoRamColorModel
import ch.scorpion.antares.view.port.DigitalPortViewStyle
import ch.scorpion.antares.view.signal.BitWidthEditor
import ch.scorpion.antares.view.signal.BitWidthParamValuePropertySwing
import ch.scorpion.antares.view.signal.BitWidthPropertySwing
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTableService
import ch.scorpion.jabbah.app.Environment
import ch.scorpion.jabbah.app.RailwayAppUsageServiceImpl
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.app.railway.AbstractRailwayAppService.Companion.PROP_PING_APPLICATION_ID
import ch.scorpion.jabbah.app.rating.RailwayRatingService
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.DataLocation
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.help.HelpSource
import ch.scorpion.jabbah.base.help.HelpSourceRegistry
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.EnumPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.sound.WaveformType
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.base.swing.ToStringRenderer
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.AbstractReflectionPropertySwing
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.view.DynamicPropertyRendererRegistry
import ch.scorpion.jabbah.execution.ExecutionModuleJvm
import ch.scorpion.jabbah.graph.container.ContainerDrawingLayouter
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
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.graph.view.oscilloscope.AbstractSignalHistoryDrawer
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap
import java.net.URL
import javax.swing.table.DefaultTableCellRenderer

/**
 * Module definitions for the [ch.scorpion.antares] module on the JVM target.
 */
class AntaresModuleJvm(private val app: AntaresDesktop) : AbstractModule() {

	companion object {
		const val PREF_TREE_CIRCUIT = "antares.preferences.group.circuit"
		const val PREF_TREE_CIRCUIT_DIGITAL = "antares.preferences.group.circuit.digital"
		const val PREF_TREE_CIRCUIT_ANALOG = "antares.preferences.group.circuit.analog"
		const val PREF_TREE_EXPRESSION = "antares.preferences.group.expression"

		val createCircuitFromTruthTableService = CreateCircuitFromTruthTableService()
	}

	override fun initialize() {
		LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

		GraphViewModule.containerEditorFactory = { DigitalContainerEditor(it) }

		GraphViewModuleJvm.containerToolBarBuilderFactory = { DigitalContainerToolBarBuilder() }
		GraphModuleJvm.containerTreeViewFactory = { DigitalContainerTreeView() }
		GraphModuleJvm.libraryTreeViewActionsProvider = {
				params -> DigitalLibraryTreeViewActionsSwing(params.controller, params.type, params.application)
		}
		GraphModuleJvm.metaGraphHistoryService = FileMetaGraphHistoryServiceImpl({ AppModuleJvm.workspaceHolder.userDataDirectoryPath })

		GraphModuleJvm.require()
		AntaresViewModule.require()

		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(
			{ AppModuleJvm.workspaceHolder.userDataDirectoryPath },
			directoryName = app.userLibraryDirectoryName,
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName,
			metaGraphHistoryService = GraphModuleJvm.metaGraphHistoryService)

		LibraryModule.systemLibraryPersistenceService = if (app.systemLibraryBasePath != null) {
			FileLibraryPersistenceService(
				{ app.systemLibraryBasePath!! },
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
			FileLibraryDictionaryPersistenceService({ AppModuleJvm.workspaceHolder.userDataDirectoryPath }, app.userLibraryDirectoryName))

		LibraryModule.systemLibraryDictionaryService = if (app.systemLibraryBasePath != null) {
			LibraryDictionaryService(FileLibraryDictionaryPersistenceService({app.systemLibraryBasePath!!}, AntaresApplication.DEFAULT_LIB_DIRECTORY))
		} else {
			LibraryDictionaryService(ResourceLibraryDictionaryPersistenceService())
		}

		LibraryModule.libraryManagementService = LibraryManagementService()

		ProjectModule.projectDictionaryService = LibraryDictionaryService(
			FileLibraryDictionaryPersistenceService({ AppModuleJvm.workspaceHolder.userDataDirectoryPath }, app.projectDirectoryName))


		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService(
			{ AppModuleJvm.workspaceHolder.userDataDirectoryPath },
			directoryName = app.projectDirectoryName,
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName,
			metaGraphHistoryService = GraphModuleJvm.metaGraphHistoryService)

		ProjectModule.projectManagementService = ProjectManagementService(
			newMetaGraphNameTranslationKey = "graph.name.unknown")

		if (app.dataLocation == DataLocation.Local) {
			GraphModuleJvm.projectAkrabClientServiceJvm = {
				val serverUrl = try {
					URL(BaseModule.properties.getString(DataLocation.PROP_SERVER_URL))
				} catch (e: Exception) {
					URL("http://localhost")
				}
				ProjectAkrabClientServiceJvm(
					serverUrl,
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
		configureMetaGraphIcons()

		buildPreferencesTree(BaseModuleJvm.preferencesTree)

		registerHelpSources()
	}

	@Suppress("SpellCheckingInspection")
	private fun customizeProperties(properties: Properties) {
		properties.set(AbstractLibraryImportProcess.PROP_PROJECT_FILE_EXTENSION, "acp") // Antares Circuit Project
		properties.set(AbstractLibraryImportProcess.PROP_LIBRARY_FILE_EXTENSION, "acl") // Antares Circuit Library
		properties.set(DigitalSignalColor.PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR, true)
		properties.set(PROP_PING_APPLICATION_ID, "498417e8-efd2-4c78-8a11-317037cc9afa")
		properties.set(RailwayAppUsageServiceImpl.PROP_PING_URL, "https://metrics.antarescircuit.io/api/ping")
		properties.set(RailwayRatingService.PROP_ASPECTS_URL, "https://metrics.antarescircuit.io/api/aspects")
		properties.set(RailwayRatingService.PROP_RATING_URL, "https://metrics.antarescircuit.io/api/rating")

		// Akrab REST API
		when (app.environment) {
			Environment.Development -> {
				properties.set(DataLocation.PROP_SERVER_URL, AntaresApplication.AKRAB_DEV_URL)
			}
			Environment.Production -> {
				properties.set(DataLocation.PROP_SERVER_URL, AntaresApplication.AKRAB_PROD_URL)
			}
			else -> throw IllegalStateException("no Akrab REST settings for environment ${app.environment}")
		}
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("text", TextComponentJvm::class)
	}

	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.registerRenderer(LightColor::class.java, LightColorRenderer::class.java)
		registry.registerRenderer(PortCount::class.java, EnumRenderer::class.java)
		registry.registerRenderer(InputPortNumber::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Handedness::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Logic::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Trigger::class.java, EnumRenderer::class.java)
		registry.registerRenderer(BranchCount::class.java, ToStringRenderer::class.java)
		registry.registerRenderer(BitWidth::class.java, ToStringRenderer::class.java)
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
		registry.registerRenderer(TunnelFlowDirection::class.java, EnumRenderer::class.java)
		registry.registerRenderer(NetSignalApplierStrategy::class.java, EnumRenderer::class.java)
		registry.registerRenderer(TunnelName::class.java, DefaultTableCellRenderer::class.java)
		registry.registerRenderer(EnterBehavior::class.java, EnumRenderer::class.java)
	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(LightColor::class.java) { LightColorEditor((it as CommandPropertySwing<LightColor>).optional) }
		registry.register(PortCount::class.java) { PortCountEditor((it as CommandPropertySwing<PortCount>).filter) }
		registry.register(InputPortNumber::class.java) { InputPortNumberEditor((it as CommandPropertySwing<InputPortNumber>).filter) }
		registry.registerEditor(Handedness::class.java, HandednessEditor::class.java)
		registry.registerEditor(Logic::class.java, LogicEditor::class.java)
		registry.registerEditor(Trigger::class.java, TriggerEditor::class.java)
		registry.register(BranchCount::class.java) { BranchCountEditor((it as CommandPropertySwing<BranchCount>).filter) }
		registry.register(DigitalSignalRepresentation::class.java) { DigitalSignalRepresentationEditor((it as CommandPropertySwing<DigitalSignalRepresentation>).filter)}
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
		registry.registerEditor(TunnelFlowDirection::class.java, TunnelFlowDirectionEditor::class.java)
		registry.registerEditor(NetSignalApplierStrategy::class.java, NetSignalApplierChoiceEditor::class.java)
		registry.register(TunnelName::class.java) { TunnelNameEditor((it as TunnelNameProperty).graph) }
		registry.registerEditor(EnterBehavior::class.java, EnterBehaviorEditor::class.java)

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
		root.getGroup(BaseModuleJvm.PREF_TREE_RENDERING).add(BooleanPreference(
			id = Look.PROP_FILL_BASIC_COMPONENTS,
			nameKey = "antares.preference.fillBasicComponents"
		))

		root.getGroup(ExecutionModuleJvm.PREF_TREE_EXECUTION).add(BooleanPreference(
			id = DigitalSignalColor.PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR,
			nameKey = "antares.preference.differentNonZeroMultiBitColor",
			needsRestart = true
		))

		root.add(buildCircuitPreferenceTree())

		root.getGroup(GraphViewModuleJvm.PREF_TREE_OSCILLOSCOPE).add(BooleanPreference(
			id = AbstractSignalHistoryDrawer.PROP_FILL_SIGNAL,
			nameKey = "antares.preference.SignalHistory.fill"
		))
	}

	private fun buildCircuitPreferenceTree(): PreferenceGroup =
		PreferenceGroup(PREF_TREE_CIRCUIT).apply {
			add(buildDigitalPreferenceTree())
			add(buildAnalogPreferenceTree())

			add(EnumPreference(
				id = SymbolStyle.PROP_SYMBOL_STYLE,
				nameKey = "antares.action.symbolStyle",
				values = SymbolStyle.values(),
				withName = SymbolStyle::withName,
				needsRestart = true
			))

			add(EnumPreference(
				id = ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER,
				nameKey = "graph.containerLayout",
				values = ContainerDrawingLayouter.values(),
				withName = ContainerDrawingLayouter::withName
			))

			add(BooleanPreference(
				id = AbstractTransistorView.PROP_TRANSISTOR_CIRCLE,
				nameKey = "antares.preference.TransistorCircle",
				needsRestart = true
			))

			add(BooleanPreference(
				id = AbstractTransistorView.PROP_TRANSISTOR_PORT_NAMES,
				nameKey = "antares.preference.TransistorPortNames",
				needsRestart = true
			))
		}

	private fun buildDigitalPreferenceTree(): PreferenceGroup =
		PreferenceGroup(PREF_TREE_CIRCUIT_DIGITAL).apply {
			add(buildDigitalExpressionsPreferenceTree())

			add(IntPreference(
				id = AbstractLogicGate.PROP_DEFAULT_PROPAGATION_DELAY,
				nameKey = "antares.preference.defaultPropagationDelay",
				minValue = 1,
				maxValue = 1_000_000,
				needsRestart = true
			))

			add(EnumPreference(
				id = UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR,
				nameKey = "antares.preference.undefinedGateInputBehavior.name",
				values = UndefinedGateInputBehavior.values(),
				withName = UndefinedGateInputBehavior::withName
			))

			add(LightColorPreference())

			add(EnumPreference(
				id = DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION,
				nameKey = "antares.preferences.DigitalSignalNotation",
				values = DigitalSignalNotation.values(),
				withName = DigitalSignalNotation::withName,
				needsRestart = true
			))

			add(EnumPreference(
				id = TunnelViewFace.PROP_TUNNEL_FACE,
				nameKey = "antares.preference.TunnelViewFace",
				values = TunnelViewFace.values(),
				withName = TunnelViewFace::withName
			))

			add(EnumPreference(
				id = TransistorViewSymbol.PROP_TRANSISTOR_SYMBOL,
				nameKey = "antares.preference.transistorSymbol.name",
				values = TransistorViewSymbol.values(),
				withName = TransistorViewSymbol::withName
			))

			add(BooleanPreference(
				id = DigitalEdgeView.PROP_WIDE_BUS_STROKE,
				nameKey = "antares.preference.wideBusStroke"
			))

			add(IntPreference(
				id = Switch.PROP_DEFAULT_DELAY,
				nameKey = "antares.preference.SwitchPropDelay",
				minValue = 0,
				maxValue = 1_000_000
			))

			add(BooleanPreference(
				id = LogicGateView.PROP_DATA_FLOW_ENABLED,
				nameKey = "antares.preferences.AndGateDataFlow"
			))
		}

	private fun buildDigitalExpressionsPreferenceTree(): PreferenceGroup =
		PreferenceGroup(PREF_TREE_EXPRESSION).apply {
			add(EnumPreference(
				id = BooleanExpressionNotation.PROP_NOTATION,
				nameKey = "antares.preference.expression.notation",
				values = BooleanExpressionNotation.values(),
				withName = BooleanExpressionNotation::withName
			))
			add(BooleanPreference(
				id = BooleanExpressionNotation.PROP_OMIT_AND,
				nameKey = "antares.preference.expression.omitAnd"
			))
			add(BooleanPreference(
				id = BooleanExpressionNotation.PROP_AND_PARENTHESIS,
				nameKey = "antares.preference.expression.andParenthesis"
			))
		}

	private fun buildAnalogPreferenceTree(): PreferenceGroup =
		PreferenceGroup(PREF_TREE_CIRCUIT_ANALOG).apply {

			add(IntPreference(
				id = AnalogEdgeView.PREF_SPEED,
				nameKey = "antares.analog.currentFlowAnimSpeed",
				minValue = AnalogEdgeView.MIN_SPEED,
				maxValue = AnalogEdgeView.MAX_SPEED))
		}

	private fun configureMetaGraphIcons() {
		MetaGraphIconProvider.register(AntaresGraphTypes.Digital, current = false, scripted = false, AntaresMetaGraphIcon(current = false, false))
		MetaGraphIconProvider.register(AntaresGraphTypes.Digital, current = true, scripted = false, AntaresMetaGraphIcon(current = true, false))
		MetaGraphIconProvider.register(AntaresGraphTypes.Digital, current = false, scripted = true, AntaresMetaGraphIcon(current = false, true))
		MetaGraphIconProvider.register(AntaresGraphTypes.Digital, current = true, scripted = true, AntaresMetaGraphIcon(current = true, true))

		MetaGraphIconProvider.register(AntaresGraphTypes.Analog, current = false, scripted = false, AnalogMetaGraphIcon(current = false, false))
		MetaGraphIconProvider.register(AntaresGraphTypes.Analog, current = true, scripted = false, AnalogMetaGraphIcon(current = true, false))
		MetaGraphIconProvider.register(AntaresGraphTypes.Analog, current = false, scripted = true, AnalogMetaGraphIcon(current = false, true))
		MetaGraphIconProvider.register(AntaresGraphTypes.Analog, current = true, scripted = true, AnalogMetaGraphIcon(current = true, true))
	}

	private fun registerHelpSources() {
		HelpSourceRegistry.register(TestcaseViewSwing.HELP_ID, HelpSource("/circuits/circuit-tests"))
		HelpSourceRegistry.register(ExportVHDLPanel.HELP_ID, HelpSource("/circuits/vhdl-export"))
	}
}