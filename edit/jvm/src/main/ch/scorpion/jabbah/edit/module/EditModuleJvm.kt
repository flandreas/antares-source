package ch.scorpion.jabbah.edit.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.DirectionEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.StyleTypeEditor
import ch.scorpion.jabbah.draw.style.StyleTypeRenderer
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.SizeEditor
import ch.scorpion.jabbah.edit.model.VerticalAlignmentEditor
import ch.scorpion.jabbah.edit.model.text.TextComponent
import ch.scorpion.jabbah.edit.model.text.TextComponentFactoryJvm
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.IOModuleJvm
import ch.scorpion.jabbah.io.TypeMap
import com.l2fprod.common.propertysheet.PropertyRendererRegistry

/**
 * Setup of the [ch.scorpion.jabbah.edit] module for the JVM target.
 */
object EditModuleJvm : AbstractModule() {

    val propertyRendererRegistry = PropertyRendererRegistry()

    val propertyEditorRegistry = DynamicPropertyEditorRegistry()

    var propertySheetPanelFactory: PropertySheetPanelFactory = PropertySheetPanelFactoryImpl(
            propertyRendererRegistry, propertyEditorRegistry)

    override fun initialize() {
        DrawModuleJvm.require()
        IOModuleJvm.require()

        registerTypes(IOModule.typeMap)

        EditModule.textComponentFactory = { TextComponentFactoryJvm() }
        EditModule.require()

        configurePropertyRenderer(propertyRendererRegistry)
        configurePropertyEditors(propertyEditorRegistry)
    }

    private fun configurePropertyRenderer(registry: PropertyRendererRegistry) {
        registry.registerRenderer(Direction::class.java, EnumRenderer::class.java)
        registry.registerRenderer(PredefinedColor::class.java, PredefinedColorRenderer::class.java)
        registry.registerRenderer(Size::class.java, EnumRenderer::class.java)
        registry.registerRenderer(StyleType::class.java, StyleTypeRenderer::class.java)
        registry.registerRenderer(VerticalAlignment::class.java, EnumRenderer::class.java)
        registry.registerRenderer(TextProperty::class.java, TextPropertyRenderer::class.java)
    }

    private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
        registry.registerEditor(Direction::class.java, DirectionEditor::class.java)
        registry.registerEditor(Size::class.java, SizeEditor::class.java)
        registry.registerEditor(StyleType::class.java, StyleTypeEditor::class.java)
        registry.registerEditor(VerticalAlignment::class.java, VerticalAlignmentEditor::class.java)
        registry.register(PredefinedColor::class.java, { PredefinedColorEditor(PredefinedColorRepository) })
        registry.register(TextProperty::class.java, TextPropertyEditorFactory())
    }

    private fun registerTypes(typeMap: TypeMap) {
        typeMap.register("text", TextComponent::class)
    }
}