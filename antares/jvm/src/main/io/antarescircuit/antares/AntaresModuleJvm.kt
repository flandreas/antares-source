package io.antarescircuit.antares

import io.antarescircuit.antares.hdl.vhdl.ExportVHDLPanel
import io.antarescircuit.antares.health.SubCircuitPortConsistencyCheck
import io.antarescircuit.antares.model.*
import io.antarescircuit.antares.model.addressable.MemoryStorableIdentification
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.fsm.FSMStateType
import io.antarescircuit.antares.model.gate.CurrentDefaultPropagationDelay
import io.antarescircuit.antares.model.gate.UndefinedGateInputBehavior
import io.antarescircuit.antares.model.input.CurrentSwitchPropagationDelay
import io.antarescircuit.antares.model.input.SwitchConfiguration
import io.antarescircuit.antares.model.net.*
import io.antarescircuit.antares.model.output.SevenSegmentDisplayScheme
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.model.testcase.CombinedTestcaseRunner
import io.antarescircuit.antares.model.testcase.TestcaseViewSwing
import io.antarescircuit.antares.model.truthtable.TruthTableServiceJvm
import io.antarescircuit.antares.model.truthtable.TruthTableServiceJvmImpl
import io.antarescircuit.antares.view.*
import io.antarescircuit.antares.view.addressable.MemoryStorableIdentificationEditor
import io.antarescircuit.antares.view.addressable.MemoryStorableIdentificationRenderer
import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.AnalogLEDView
import io.antarescircuit.antares.view.analog.CurrentFlowAnimationSpeedSlider
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.container.DigitalContainerEditor
import io.antarescircuit.antares.view.container.DigitalContainerToolBarBuilder
import io.antarescircuit.antares.view.container.DigitalContainerTreeView
import io.antarescircuit.antares.view.expression.BooleanExpressionDesktopItemSwing
import io.antarescircuit.antares.view.fsm.FSMGraphDesktopItemSwing
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.graph.AnalogMetaGraphIcon
import io.antarescircuit.antares.view.graph.AntaresMetaGraphIcon
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.net.AbstractTransistorView
import io.antarescircuit.antares.view.net.DigitalEdgeView
import io.antarescircuit.antares.view.net.TransistorViewSymbol
import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.antares.view.net.tunnel.TunnelNameEditor
import io.antarescircuit.antares.view.net.tunnel.TunnelNameProperty
import io.antarescircuit.antares.view.net.tunnel.TunnelViewFace
import io.antarescircuit.antares.view.output.*
import io.antarescircuit.antares.view.port.DigitalPortViewStyle
import io.antarescircuit.antares.view.signal.BitWidthEditor
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.antares.view.synthesis.CreateCircuitFromTruthTableService
import io.antarescircuit.antares.view.truthtable.TruthTableDesktopItemSwing
import io.antarescircuit.jabbah.app.Environment
import io.antarescircuit.jabbah.app.RailwayAppUsageServiceImpl
import io.antarescircuit.jabbah.app.health.SystemHealthChecker
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.app.railway.AbstractRailwayAppService.Companion.PROP_PING_APPLICATION_ID
import io.antarescircuit.jabbah.app.rating.RailwayRatingService
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.DataLocation
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.help.HelpSource
import io.antarescircuit.jabbah.base.help.HelpSourceRegistry
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.preferences.*
import io.antarescircuit.jabbah.base.sound.WaveformType
import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.model.text.TextComponentJvm
import io.antarescircuit.jabbah.edit.module.EditModuleJvm
import io.antarescircuit.jabbah.edit.properties.AbstractReflectionPropertySwing
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.DynamicPropertyEditorRegistry
import io.antarescircuit.jabbah.edit.view.DynamicPropertyRendererRegistry
import io.antarescircuit.jabbah.execution.ExecutionModuleJvm
import io.antarescircuit.jabbah.graph.container.ContainerDrawingLayouter
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileServiceJvm
import io.antarescircuit.jabbah.graph.model.param.*
import io.antarescircuit.jabbah.graph.module.GraphModuleJvm
import io.antarescircuit.jabbah.graph.project.ProjectAkrabClientServiceJvm
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewHeaderFactory
import io.antarescircuit.jabbah.graph.ui.MetaGraphIconProvider
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.module.GraphViewModuleJvm
import io.antarescircuit.jabbah.graph.view.oscilloscope.AbstractSignalHistoryDrawer
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap
import java.net.URL
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Module definitions for the [io.antarescircuit.antares] module on the JVM target.
 */
class AntaresModuleJvm(private val app: AntaresDesktop) : AbstractModule() {

	companion object {
		const val PREF_TREE_CIRCUIT = "antares.preferences.group.circuit"
		const val PREF_TREE_CIRCUIT_DIGITAL = "antares.preferences.group.circuit.digital"
		const val PREF_TREE_CIRCUIT_ANALOG = "antares.preferences.group.circuit.analog"
		const val PREF_TREE_EXPRESSION = "antares.preferences.group.expression"
		const val PREF_TREE_TEST_CASES = "antares.preferences.group.testcase"

		val createCircuitFromTruthTableService = CreateCircuitFromTruthTableService()

		val truthTableServiceJvm: TruthTableServiceJvm = TruthTableServiceJvmImpl()
	}

	override fun initialize() {
		LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

		GraphViewModule.containerEditorFactory = { dv1, dv2 -> DigitalContainerEditor(dv1, dv2) }

		GraphViewModuleJvm.containerToolBarBuilderFactory = { DigitalContainerToolBarBuilder() }
		GraphModuleJvm.containerTreeViewFactory = { dv -> DigitalContainerTreeView(dv) }
		GraphModuleJvm.libraryTreeViewActionsProvider = {
				params -> DigitalLibraryTreeViewActionsSwing(params.controller, params.type, params.application)
		}
		GraphModuleJvm.metaGraphHistoryService = FileMetaGraphHistoryServiceImpl({ AppModuleJvm.workspaceHolder.userDataDirectoryPath })

		GraphModelModule.nonVolatileService = NonVolatileServiceJvm(
			{ AppModuleJvm.workspaceHolder.userDataDirectoryPath },
			app.nonVolatileDirectoryName
		)

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

		GraphViewModuleJvm.libraryPreferencesProvider = {
			GraphViewModuleJvm.getLibraryPreferences().apply {
				add(BaseModuleJvm.preferencesTree
					.getGroup(PREF_TREE_CIRCUIT)
					.getGroup(PREF_TREE_CIRCUIT_DIGITAL)
					.get(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR))

				add(BaseModuleJvm.preferencesTree
					.getGroup(PREF_TREE_CIRCUIT)
					.getGroup(PREF_TREE_CIRCUIT_DIGITAL)
					.get(CurrentDefaultPropagationDelay.PROP_DEFAULT_PROPAGATION_DELAY))

				add(BaseModuleJvm.preferencesTree
					.getGroup(PREF_TREE_CIRCUIT)
					.getGroup(PREF_TREE_CIRCUIT_DIGITAL)
					.get(CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY))
			}
		}

		if (app.dataLocation == DataLocation.Local) {
			GraphModuleJvm.projectAkrabClientService = {
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

		GraphModuleJvm.graphNavigationViewHeaderFactory = object : GraphNavigationViewHeaderFactory {
			override fun createHeader(graphView: GraphView): JPanel? {
				if (graphView.graph?.type == AntaresGraphTypes.Analog) {
					val header = JPanel()
					header.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
					header.layout = BoxLayout(header, BoxLayout.X_AXIS)

					header.add(CurrentFlowAnimationSpeedSlider())
					header.add(Box.createGlue())

					return header
				}
				return null
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

		if (EditAuthModule.userHolder.user.isDeveloper || AppModuleJvm.remoteControlService.getBoolean(GraphViewConsistencyCheck.REMOTE_PROP_CONSISTENCY_CHECK)) {
			SystemHealthChecker.register(SubCircuitPortConsistencyCheck)
		}
	}

	override fun resetDependencies() {
		GraphModuleJvm.reset()
		AntaresViewModule.reset()
	}

	@Suppress("SpellCheckingInspection")
	private fun customizeProperties(properties: Properties) {
		properties.set(AbstractLibraryImportProcess.PROP_PROJECT_FILE_EXTENSION, "acp") // Antares Circuit Project
		properties.set(AbstractLibraryImportProcess.PROP_LIBRARY_FILE_EXTENSION, "acl") // Antares Circuit Library
		properties.set(PROP_PING_APPLICATION_ID, "498417e8-efd2-4c78-8a11-317037cc9afa")
		properties.set(RailwayAppUsageServiceImpl.PROP_PING_URL, "https://metrics.antarescircuit.io/api/ping")
		properties.set(RailwayRatingService.PROP_ASPECTS_URL, "https://metrics.antarescircuit.io/api/aspects")
		properties.set(RailwayRatingService.PROP_RATING_URL, "https://metrics.antarescircuit.io/api/rating")
		properties.set(LibraryService.PROP_VIEWER_JS_URL, AntaresApplication.ANTARES_VIEWER_JS_URL)

		// Akrab REST API
		when (app.environment) {
			Environment.Development -> {
				properties.set(DataLocation.PROP_SERVER_URL, AntaresApplication.AKRAB_DEV_URL)
			}
			Environment.Production -> {
				properties.set(DataLocation.PROP_SERVER_URL, AntaresApplication.AKRAB_PROD_URL)
			}
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
		registry.registerRenderer(DigitalSignalRepresentation::class.java, DigitalSignalRepresentationRenderer::class.java)
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
		registry.registerRenderer(MemoryStorableIdentification::class.java, MemoryStorableIdentificationRenderer::class.java)
		registry.registerRenderer(SwitchConfiguration::class.java, EnumRenderer::class.java)
		registry.registerRenderer(AnalogOscilloscopeSignalType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(FSMStateType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(LEDShape::class.java, EnumRenderer::class.java)
	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(LightColor::class.java) { LightColorEditor((it as CommandPropertySwing<LightColor>).optional) }
		registry.register(PortCount::class.java) { PortCountEditor((it as CommandPropertySwing<PortCount>).filter) }
		registry.register(InputPortNumber::class.java) { InputPortNumberEditor((it as CommandPropertySwing<InputPortNumber>).filter) }
		registry.registerEditor(Handedness::class.java, HandednessEditor::class.java)
		registry.registerEditor(Logic::class.java, LogicEditor::class.java)
		registry.registerEditor(Trigger::class.java, TriggerEditor::class.java)
		registry.register(BranchCount::class.java) { BranchCountEditor((it as CommandPropertySwing<BranchCount>).filter) }
		registry.register(DigitalSignalRepresentation::class.java) {
			DigitalSignalRepresentationEditor(
				(it as CommandPropertySwing<DigitalSignalRepresentation>).optional,
				it.filter)
		}
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
		registry.registerEditor(MemoryStorableIdentification::class.java, MemoryStorableIdentificationEditor::class.java)
		registry.registerEditor(SwitchConfiguration::class.java, SwitchConfigurationEditor::class.java)
		registry.registerEditor(AnalogOscilloscopeSignalType::class.java, AnalogOscilloscopeSignalTypeEditor::class.java)
		registry.registerEditor(FSMStateType::class.java, FSMStateTypeEditor::class.java)
		registry.registerEditor(LEDShape::class.java, LEDShapeEditor::class.java)

		registry.register(BitWidth::class.java) { prop ->
			BitWidthEditor(
				propertyName = prop.displayName,
				editable = (prop as ExpressionPropertySwing<BitWidth>).editable,
				supportExpressions = prop.supportExpressions,
				graphEditor = prop.editor,
				errorCallback = { prop.dslError = it },
				filter = prop.filter )
		}

		registry.register(LightColor::class.java) { prop ->
			if (prop is ExpressionPropertySwing<*>) {
				LightColorExpressionEditor(
					propertyName = prop.displayName,
					editable = (prop as ExpressionPropertySwing<LightColor>).editable,
					supportExpressions = prop.supportExpressions,
					graphEditor = prop.editor,
					errorCallback = { prop.dslError = it },
					filter = prop.filter)
			} else if (prop is DefaultLightColorProperty) {
				LightColorEditor(optional = true)
			} else {
				LightColorExpressionEditor(
					propertyName = prop.displayName,
					editable = (prop as CommandPropertySwing<LightColor>).editable,
					supportExpressions = false,
					graphEditor = prop.editor,
					errorCallback = { },
					filter = prop.filter)
			}
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
					return GraphParamValuePropertySwing(
						paramDefinition = def as GraphParamDefinition<BitWidth>,
						propertyName = "BitWidth", // only used for logging
						BitWidth::class.java,
						beanProvider
					)
				}
			}
		)

		GraphParamValuePropertyFactoryRegistry.register(
			LightColorGraphParamType,
			object : GraphParamValuePropertyFactory {
				override fun create(
					def: GraphParamDefinition<*>,
					editor: Editor,
					beanProvider: BeanProvider
				): AbstractReflectionPropertySwing<*> {
					return GraphParamValuePropertySwing(
						paramDefinition = def as GraphParamDefinition<LightColor>,
						propertyName = "LightColor",
						LightColor::class.java,
						beanProvider
					)
				}
			}
		)
	}

	private fun configureGraphParamValueEditors() {
		GraphParamValueEditorRegistry.register(BitWidthGraphParamType) { BitWidthGraphParamValueEditor() }
		GraphParamValueEditorRegistry.register(LightColorGraphParamType) { LightColorGraphParamValueEditor() }
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
			add(buildTestcasesPreferenceTree())

			add(EnumPreference(
				id = SymbolStyle.PROP_SYMBOL_STYLE,
				nameKey = "antares.action.symbolStyle",
				values = SymbolStyle.entries.toTypedArray(),
				withName = SymbolStyle::withName,
				needsRestart = true
			))

			add(EnumPreference(
				id = ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER,
				nameKey = "graph.containerLayout",
				values = ContainerDrawingLayouter.entries.toTypedArray(),
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

			add(BooleanPreference(
				id = SymbolStyle.PROP_TRI_STATE_ALWAYS_TRIANGLE,
				nameKey = "antares.preference.TriStateAlwaysTriangle",
				needsRestart = true
			))
			add(BooleanPreference(
				id = DigitalPort.PROP_ADJUST_BIT_WIDTH,
				nameKey = "antares.preference.adjustBitWidth"
			))

			add(BooleanPreference(
				id = GraphPropagationDelayCalculator.PROP_CALCULATE_ON_SAVE,
				nameKey = "graph.preferences.calculatePropDelayUponSave",
				needsRestart = true
			))
		}

	private fun buildTestcasesPreferenceTree(): PreferenceGroup =
		PreferenceGroup(PREF_TREE_TEST_CASES).apply {
			add(BooleanPreference(
				id = CombinedTestcaseRunner.PROP_CHECK_PROP_DELAY_CONSISTENCY,
				nameKey = "antares.preferences.checkPropDelayConsistency.name"
			))
		}

	private fun buildDigitalPreferenceTree(): PreferenceGroup =
		PreferenceGroup(PREF_TREE_CIRCUIT_DIGITAL).apply {
			add(buildDigitalExpressionsPreferenceTree())

			add(IntPreference(
				id = CurrentDefaultPropagationDelay.PROP_DEFAULT_PROPAGATION_DELAY,
				nameKey = "antares.preference.defaultPropagationDelay",
				minValue = 1,
				maxValue = 1_000_000,
				needsRestart = true
			))

			add(EnumPreference(
				id = UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR,
				nameKey = "antares.preference.undefinedGateInputBehavior.name",
				values = UndefinedGateInputBehavior.entries.toTypedArray(),
				withName = UndefinedGateInputBehavior::withName
			))

			add(LightColorPreference())

			add(EnumPreference(
				id = DigitalSignalRepresentation.PROP_DEFAULT_SIGNAL_REPRESENTATION,
				nameKey = "antares.preferences.SignalRepresentation",
				values = DigitalSignalRepresentation.entries.toTypedArray(),
				withName = DigitalSignalRepresentation::withName,
				needsRestart = true
			))

			add(EnumPreference(
				id = DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION,
				nameKey = "antares.preferences.DigitalSignalNotation",
				values = DigitalSignalNotation.entries.toTypedArray(),
				withName = DigitalSignalNotation::withName,
				needsRestart = true
			))

			add(EnumPreference(
				id = TunnelViewFace.PROP_TUNNEL_FACE,
				nameKey = "antares.preference.TunnelViewFace",
				values = TunnelViewFace.entries.toTypedArray(),
				withName = TunnelViewFace::withName
			))

			add(EnumPreference(
				id = TransistorViewSymbol.PROP_TRANSISTOR_SYMBOL,
				nameKey = "antares.preference.transistorSymbol.name",
				values = TransistorViewSymbol.entries.toTypedArray(),
				withName = TransistorViewSymbol::withName
			))

			add(BooleanPreference(
				id = DigitalEdgeView.PROP_WIDE_BUS_STROKE,
				nameKey = "antares.preference.wideBusStroke"
			))

			add(IntPreference(
				id = CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY,
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
				values = BooleanExpressionNotation.entries.toTypedArray(),
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

			add(FloatPreference(
				id = AnalogCircuitAnalysis.PROP_TIME_STEP,
				nameKey = "antares.analog.timeStep.name",
				minValue = AnalogCircuitAnalysis.MIN_TIME_STEP,
				maxValue = AnalogCircuitAnalysis.MAX_TIME_STEP)
			)

			add(BooleanPreference(
				id = AnalogLEDView.PROP_DRAW_HALO,
				nameKey = "antares.preference.analogLED.drawHalo"
			))
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
		HelpSourceRegistry.register(TruthTableDesktopItemSwing.HELP_ID, HelpSource("/circuits/synthesis#chapter-truth-tables"))
		HelpSourceRegistry.register(BooleanExpressionDesktopItemSwing.HELP_ID, HelpSource("/circuits/synthesis#chapter-boolean-expressions"))
		HelpSourceRegistry.register(FSMGraphDesktopItemSwing.HELP_ID, HelpSource("/fsm/fsm"))
	}
}