package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.FloatPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.edit.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.view.DynamicPropertyRendererRegistry
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import ch.scorpion.jabbah.io.IOModuleJvm

/**
 * Module definitions for the [ch.scorpion.jabbah.graph] module on the JVM platform.
 */
object GraphViewModuleJvm : AbstractModule() {

	override fun initialize() {
		IOModuleJvm.require()
		DrawModuleJvm.require()
		GraphViewModule.require()

		fillProperties(BaseModule.properties)
		configureSelectionModels(EditSelectModule.selectionModelFactory)
		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(GraphDesktopItemHeaderPanel.PROP_BACKGROUND_COLOR, Color(214, 214, 214))
		properties.set(NavigationStackViewSwing.PROP_FONT, FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 11))
		properties.set(NavigationStackViewSwing.PROP_HEAD_FONT, FontImpl(FontFamily.SANS_SERIF, FontStyle.BOLD.value, 11))
		properties.set(NavigationStackViewSwing.PROP_BACKGROUND_COLOR, Color.WHITE)
		properties.set(NavigationStackViewSwing.PROP_BORDER_COLOR, Color(214, 214, 214))
		properties.set(NavigationStackViewSwing.PROP_TEXT_COLOR, Color.BLACK)
		properties.set(NavigationStackViewSwing.PROP_HOVER_BACKGROUND_COLOR, Color.GRAY)
		properties.set(NavigationStackViewSwing.PROP_HOVER_BORDER_COLOR, Color.GRAY)
		properties.set(NavigationStackViewSwing.PROP_HOVER_TEXT_COLOR, Color.WHITE)
		properties.set(NavigationStackViewSwing.PROP_HEAD_BACKGROUND_COLOR, Color.WHITE)
		properties.set(NavigationStackViewSwing.PROP_HEAD_BORDER_COLOR, Color(214, 214, 214))
		properties.set(NavigationStackViewSwing.PROP_HEAD_TEXT_COLOR, Color.BLACK)
		properties.set(GraphNavigationPanel.PROP_DIVE_ANIMATION, true)
		properties.set(GraphFrameController.PROP_AUTO_SWITCH, true)
	}

	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.registerRenderer(LayoutType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PortType::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PortLabelPosition::class.java, EnumRenderer::class.java)
		registry.registerRenderer(VerticeLabelPosition::class.java, EnumRenderer::class.java)
		registry.registerRenderer(NetViewStyle::class.java, EnumRenderer::class.java)

	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.registerEditor(LayoutType::class.java, LayoutEditor::class.java)
		registry.registerEditor(PortType::class.java, PortTypeEditor::class.java)
		registry.registerEditor(PortLabelPosition::class.java, PortLabelPositionEditor::class.java)
		registry.registerEditor(VerticeLabelPosition::class.java, VerticeLabelPositionEditor::class.java)
		registry.registerEditor(NetViewStyle::class.java, NetViewStyleEditor::class.java)
	}

	private fun configureSelectionModels(factory: SelectionModelFactory) {
		factory.register(SelectionDrawingStrategy.ABOVE, TextComponentJvm::class.simpleName!!) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.getGroup(DrawModuleJvm.PREF_TREE_VIEW).getGroup(DrawModuleJvm.PREF_TREE_VIEW_NAVIGATION).add(BooleanPreference(
			id = GraphNavigationPanel.PROP_DIVE_ANIMATION,
			nameKey = "graph.preferences.GraphNavigationPanel.diveAnimation"
		))
		root.getGroup(DrawModuleJvm.PREF_TREE_VIEW).getGroup(DrawModuleJvm.PREF_TREE_VIEW_NAVIGATION).add(BooleanPreference(
			id = GraphFrameController.PROP_AUTO_SWITCH,
			nameKey = "graph.preferences.GraphFrame.autoSwitch"
		))
		root.getGroup(DrawModuleJvm.PREF_TREE_VIEW).add(FloatPreference(
			id = ContainerEditor.PROP_DEFAULT_ZOOM_FACTOR,
			nameKey = "graph.preferences.ContainerEditor.defaultZoomFactor"
		))
	}
}