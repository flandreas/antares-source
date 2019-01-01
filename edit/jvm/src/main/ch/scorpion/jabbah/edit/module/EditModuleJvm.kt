package ch.scorpion.jabbah.edit.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.DirectionEditor
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.StyleTypeEditor
import ch.scorpion.jabbah.draw.style.StyleTypeEditorFx
import ch.scorpion.jabbah.draw.style.StyleTypeRenderer
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.SizeEditor
import ch.scorpion.jabbah.edit.model.VerticalAlignmentEditor
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularReplaceSelectionModel
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.view.DynamicPropertyRendererRegistry
import ch.scorpion.jabbah.edit.view.EditContextMenuProvider
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.IOModuleJvm
import ch.scorpion.jabbah.io.TypeMap

/**
 * Setup of the [ch.scorpion.jabbah.edit] module for the JVM target.
 */
object EditModuleJvm : AbstractModule() {

	const val PREF_TREE_EDITOR = "edit.preferences.group.editor"

	val propertyRendererRegistry = DynamicPropertyRendererRegistry()

    val propertyEditorRegistry = DynamicPropertyEditorRegistry()

	val propertyEditorRegistryFx = PropertyEditorRegistryFx()

    var propertySheetPanelFactory: PropertySheetPanelFactory = PropertySheetPanelFactoryImpl(
            propertyRendererRegistry, propertyEditorRegistry)

    override fun initialize() {
        DrawModuleJvm.require()
        IOModuleJvm.require()

        registerTypes(IOModule.typeMap)

        EditModule.require()
	    EditModule.copyPasteUtility = CopyPasteUtilityFx()

	    DrawModuleJvm.contextMenuProvider = EditContextMenuProvider()

        configurePropertyRenderer(propertyRendererRegistry)
        configurePropertyEditors(propertyEditorRegistry)

	    registerPropertyEditorsFx(propertyEditorRegistryFx)
	    registerSelectionModels()

	    buildPropertyTree(BaseModuleJvm.preferencesTree)
    }

    private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.registerRenderer(Direction::class.java, EnumRenderer::class.java)
        registry.registerRenderer(PredefinedColor::class.java, PredefinedColorRenderer::class.java)
	    registry.registerRenderer(PredefinedStroke::class.java, PredefinedStrokeRenderer::class.java)
        registry.registerRenderer(Size::class.java, EnumRenderer::class.java)
        registry.registerRenderer(StyleType::class.java, StyleTypeRenderer::class.java)
        registry.registerRenderer(VerticalAlignment::class.java, EnumRenderer::class.java)
        registry.registerRenderer(TextProperty::class.java, TextPropertyRenderer::class.java)
	    registry.register(TranslatableText::class.java) { TranslatableTextPropertyRenderer((it as PropertyImpl<TranslatableText>).filter)}
}

    private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
        registry.registerEditor(Direction::class.java, DirectionEditor::class.java)
        registry.registerEditor(Size::class.java, SizeEditor::class.java)
        registry.registerEditor(StyleType::class.java, StyleTypeEditor::class.java)
        registry.registerEditor(VerticalAlignment::class.java, VerticalAlignmentEditor::class.java)
        registry.register(PredefinedColor::class.java) { PredefinedColorEditor(PredefinedColorRepository) }
	    registry.register(PredefinedStroke::class.java) { PredefinedStrokeEditor(PredefinedStrokeRepository) }
	    registry.register(TextProperty::class.java) { TextPropertyEditor(
		    propertyName = (it as PropertyImpl<TranslatableText>).displayName)
	    }
	    registry.register(TranslatableText::class.java) { TranslatableTextPropertyEditor(
		    propertyName = (it as PropertyImpl<TranslatableText>).displayName,
		    multiline = it.filter)
	    }
    }

	private fun registerPropertyEditorsFx(registry: PropertyEditorRegistryFx) {
		registry.register(StyleType::class.java, StyleTypeEditorFx::class.java)
		registry.register(PredefinedColor::class.java, PredefinedColorEditorFx::class.java)
	}

    private fun registerTypes(typeMap: TypeMap) {
        typeMap.register("text", TextComponent::class)
    }

    private fun registerSelectionModels() {
	    EditSelectModule.selectionModelFactory.register(
		    SelectionDrawingStrategy.REPLACE,
		    TextComponentJvm::class.simpleName!!
	    ) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent, drawStrategy = RectangularReplaceSelectionModel.DrawStrategy.COMPONENT) }
    }

	private fun buildPropertyTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_EDITOR))

		root.getGroup(PREF_TREE_EDITOR).add(IntPreference(
			id = Grid.PROP_GRID_DEFAULT_DISTANCE,
			nameKey = "edit.preferences.Grid.dist",
			minValue = 1
		))

		root.getGroup(PREF_TREE_EDITOR).add(IntPreference(
			id = Grid.PROP_GRID_DEFAULT_PAINT_FACTOR,
			nameKey = "edit.preferences.Grid.paintFactor",
			minValue = 1
		))

		root.getGroup(PREF_TREE_EDITOR).add(IntPreference(
			id = Grid.PROP_GRID_MIN_DISTANCE,
			nameKey = "edit.preferences.Grid.minDist",
			minValue = 2
		))
	}
}