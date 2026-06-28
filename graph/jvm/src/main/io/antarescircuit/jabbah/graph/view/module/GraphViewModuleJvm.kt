package io.antarescircuit.jabbah.graph.view.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.help.HelpSource
import io.antarescircuit.jabbah.base.help.HelpSourceRegistry
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.preferences.*
import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm.PREF_TREE_VIEW_ZOOM_PAN
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.model.rectangle.AbstractRectangularComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import io.antarescircuit.jabbah.edit.model.text.TextComponentJvm
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.module.EditModuleJvm
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.DynamicPropertyEditorRegistry
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.select.SelectionModelFactory
import io.antarescircuit.jabbah.edit.view.DynamicPropertyRendererRegistry
import io.antarescircuit.jabbah.execution.ExecutionModuleJvm
import io.antarescircuit.jabbah.graph.container.ContainerEditor
import io.antarescircuit.jabbah.graph.container.ContainerToolBarBuilder
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryVisibility
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.oscilloscope.Oscilloscope
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.model.port.InconsistentNetError
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.ui.container.SymbolComparatorViewSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioViewSwing
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseViewSwing
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import io.antarescircuit.jabbah.graph.view.net.edge.LayoutType
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.ControlViewVisibility
import io.antarescircuit.jabbah.graph.view.vertice.VerticeLabelPosition
import io.antarescircuit.jabbah.io.IOModuleJvm

/**
 * Module definitions for the [io.antarescircuit.jabbah.graph] module on the JVM platform.
 */
object GraphViewModuleJvm : AbstractModule() {

	const val PREF_TREE_OSCILLOSCOPE = "graph.preferences.group.oscilloscope"

	var containerToolBarBuilderFactory: () -> ContainerToolBarBuilder = { ContainerToolBarBuilder() }

	/** Provides the [Preference]s from the base [Properties] the user can overwrite on a [Library].*/
	var libraryPreferencesProvider: () -> MutableList<Preference> = {
		getLibraryPreferences()
	}

	fun getLibraryPreferences(): MutableList<Preference> {
		return mutableListOf(
			BaseModuleJvm.preferencesTree.getGroup(ExecutionModuleJvm.PREF_TREE_EXECUTION).get(InconsistentNetError.PROP_ALLOWED_DURATION)
		)
	}

	override fun initialize() {
		IOModuleJvm.require()
		DrawModuleJvm.require()

		EditModule.copyPasteService = GraphViewCopyPasteService()

		GraphViewModule.require()

		fillProperties(BaseModule.properties)
		configureSelectionModels(EditSelectModule.selectionModelFactory)
		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)

		buildPreferencesTree(BaseModuleJvm.preferencesTree)

		registerHelpResources()
	}

	override fun resetDependencies() {
		IOModuleJvm.reset()
		DrawModuleJvm.reset()
		GraphViewModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		properties.set(GraphFrameController.PROP_AUTO_SWITCH, true)
		properties.set(GraphDesktopView.PROP_DOCKING, true)
		properties.set(GraphDesktopView.PROP_ROWS_PER_COLUMN, 2)
	}

	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.registerRenderer(LayoutType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PortType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PortLabelPosition::class.java, EnumRenderer::class.java)
		registry.registerRenderer(InternalLabelOrientation::class.java, EnumRenderer::class.java)
		registry.registerRenderer(VerticeLabelPosition::class.java, EnumRenderer::class.java)
		registry.registerRenderer(NetViewStyle::class.java, EnumRenderer::class.java)
		registry.registerRenderer(ControlViewVisibility::class.java, EnumRenderer::class.java)
		registry.registerRenderer(LibraryVisibility::class.java, EnumRenderer::class.java)
		registry.registerRenderer(SignalHistoriesType::class.java, EnumRenderer::class.java)
	}

	@Suppress("UNCHECKED_CAST")
	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(LayoutType::class.java) { LayoutEditor((it as CommandPropertySwing<LayoutType>).filter) }
		registry.registerEditor(PortType::class.java, PortTypeEditor::class.java)
		registry.registerEditor(PortLabelPosition::class.java, PortLabelPositionEditor::class.java)
		registry.registerEditor(InternalLabelOrientation::class.java, InternalLabelOrientationEditor::class.java)
		registry.registerEditor(VerticeLabelPosition::class.java, VerticeLabelPositionEditor::class.java)
		registry.register(NetViewStyle::class.java) { NetViewStyleEditor((it as CommandPropertySwing<NetViewStyle>).filter) }
		registry.registerEditor(ControlViewVisibility::class.java, ControlViewVisibilityEditor::class.java)
		registry.registerEditor(LibraryVisibility::class.java, LibraryVisibilityEditor::class.java)
		registry.registerEditor(SignalHistoriesType::class.java, SignalHistoriesTypeEditor::class.java)
	}

	private fun configureSelectionModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.ABOVE, TextComponentJvm::class) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.getGroup(BaseModuleJvm.PREF_TREE_VIEW).getGroup(DrawModuleJvm.PREF_TREE_VIEW_NAVIGATION).add(BooleanPreference(
			id = GraphNavigationViewController.PROP_DIVE_ANIMATION,
			nameKey = "graph.preferences.GraphNavigationPanel.diveAnimation"
		))
		root.getGroup(BaseModuleJvm.PREF_TREE_VIEW).getGroup(DrawModuleJvm.PREF_TREE_VIEW_NAVIGATION).add(BooleanPreference(
			id = GraphFrameController.PROP_AUTO_SWITCH,
			nameKey = "graph.preferences.GraphFrame.autoSwitch"
		))
		root.getGroup(BaseModuleJvm.PREF_TREE_VIEW).getGroup(DrawModuleJvm.PREF_TREE_VIEW_NAVIGATION).add(BooleanPreference(
			id = GraphDesktopView.PROP_DOCKING,
			nameKey = "graph.preferences.GraphDesktopView.docking",
			needsRestart = true
		))
		root.getGroup(BaseModuleJvm.PREF_TREE_VIEW).getGroup(DrawModuleJvm.PREF_TREE_VIEW_NAVIGATION).add(IntPreference(
			id = GraphDesktopView.PROP_ROWS_PER_COLUMN,
			nameKey = "graph.preferences.GraphDesktopView.rowsPerColumn",
			needsRestart = true,
			minValue = 1,
			maxValue = 5
		))
		root.getGroup(BaseModuleJvm.PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(FloatPreference(
			id = ContainerEditor.PROP_DEFAULT_ZOOM_FACTOR,
			nameKey = "graph.preferences.ContainerEditor.defaultZoomFactor"
		))

		root.add(PreferenceGroup(PREF_TREE_OSCILLOSCOPE))
		root.getGroup(PREF_TREE_OSCILLOSCOPE).add(IntPreference(
			id = Oscilloscope.PROP_BUFFER_SIZE,
			nameKey = "graph.preferences.Oscilloscope.bufferSize",
			minValue = 10,
			maxValue = 500
		))
		root.getGroup(PREF_TREE_OSCILLOSCOPE).add(BooleanPreference(
			id = OscilloscopeView.PROP_INDIVIDUAL_PROBE_COLORS,
			nameKey = "graph.preferences.Oscilloscope.useRefColors",
			needsRestart = true
		))
	}

	private fun registerHelpResources() {
		HelpSourceRegistry.register(ScenarioViewSwing.HELP_ID, HelpSource("/scenarios/scenarios"))
		HelpSourceRegistry.register(UsecaseViewSwing.HELP_ID, HelpSource("/subcircuits/usecases"))
		HelpSourceRegistry.register(SymbolComparatorViewSwing.HELP_ID, HelpSource("/subcircuits/subcircuits#symbol-comparison"))
	}
}