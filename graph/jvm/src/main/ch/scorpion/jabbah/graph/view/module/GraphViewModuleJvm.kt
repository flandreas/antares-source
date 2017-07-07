package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.edit.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.script.ScriptEngineJvm
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.ui.NavigationStackView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPropertyEditorFactory
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioStepPropertyEditorFactory
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.Layout
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.io.IOModuleJvm
import com.l2fprod.common.propertysheet.PropertyRendererRegistry

/**
 * Module definitions for the [ch.scorpion.jabbah.graph] module on the JVM platform.
 */
object GraphViewModuleJvm : AbstractModule() {

    override fun initialize() {
        IOModuleJvm.require()
        DrawModuleJvm.require()
        GraphViewModule.require()
        ScriptModule.scriptEngineProvider = { ScriptEngineJvm() }

        fillProperties(BaseModule.properties)
        configureSelectionModels(EditSelectModule.selectionModelFactory)
        configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
        configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)
    }

    private fun fillProperties(properties: Properties) {
        properties.predefine(NavigationStackView.PROP_FONT, FontImpl("Dialog", FontStyle.PLAIN.value, 11))
        properties.predefine(NavigationStackView.PROP_HEAD_FONT, FontImpl("Dialog", FontStyle.BOLD.value, 11))
        properties.predefine(NavigationStackView.PROP_BACKGROUND_COLOR, Color.WHITE)
        properties.predefine(NavigationStackView.PROP_BORDER_COLOR, Color(224, 224, 224))
        properties.predefine(NavigationStackView.PROP_TEXT_COLOR, Color.BLACK)
        properties.predefine(NavigationStackView.PROP_HOVER_BACKGROUND_COLOR, Color.GRAY)
        properties.predefine(NavigationStackView.PROP_HOVER_BORDER_COLOR, Color.GRAY)
        properties.predefine(NavigationStackView.PROP_HOVER_TEXT_COLOR, Color.WHITE)
        properties.predefine(NavigationStackView.PROP_HEAD_BACKGROUND_COLOR, Color.LIGHT_GRAY)
        properties.predefine(NavigationStackView.PROP_HEAD_BORDER_COLOR, Color.LIGHT_GRAY)
        properties.predefine(NavigationStackView.PROP_HEAD_TEXT_COLOR, Color.BLACK)
    }

    private fun configurePropertyRenderer(registry: PropertyRendererRegistry) {
        registry.registerRenderer(Layout::class.java, EnumRenderer::class.java)
        registry.registerRenderer(PortType::class.java, EnumRenderer::class.java)
        registry.registerRenderer(PortLabelPosition::class.java, EnumRenderer::class.java)
        registry.registerRenderer(NetViewStyle::class.java, EnumRenderer::class.java)

    }

    private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
        registry.registerEditor(Layout::class.java, LayoutEditor::class.java)
        registry.registerEditor(PortType::class.java, PortTypeEditor::class.java)
        registry.registerEditor(PortLabelPosition::class.java, PortLabelPositionEditor::class.java)
        registry.registerEditor(NetViewStyle::class.java, NetViewStyleEditor::class.java)
        registry.register(Scenario::class.java, ScenarioPropertyEditorFactory())
        registry.register(ScenarioStep::class.java, ScenarioStepPropertyEditorFactory())
    }

    private fun configureSelectionModels(factory: SelectionModelFactory) {
        factory.register(SelectionDrawingStrategy.ABOVE, GraphTextComponent::class.simpleName!!, { RectangularHandleSelectionModel(it as RectangularComponent) })
    }
}