package io.antarescircuit.jabbah.edit.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.DirectionEditor
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.preferences.EnumPreference
import io.antarescircuit.jabbah.base.preferences.IntPreference
import io.antarescircuit.jabbah.base.preferences.PreferenceGroup
import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.draw.graphics.*
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.style.StyleTypeEditor
import io.antarescircuit.jabbah.draw.style.StyleTypeRenderer
import io.antarescircuit.jabbah.edit.Grid
import io.antarescircuit.jabbah.edit.GridType
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.model.rectangle.AbstractRectangularComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularReplaceSelectionModel
import io.antarescircuit.jabbah.edit.model.text.*
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.properties.*
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.select.RubberBandHandler
import io.antarescircuit.jabbah.edit.view.*
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.IOModuleJvm
import io.antarescircuit.jabbah.io.TypeMap
import javax.swing.table.DefaultTableCellRenderer

/**
 * Setup of the [io.antarescircuit.jabbah.edit] module for the JVM target.
 */
object EditModuleJvm : AbstractModule() {

	const val PREF_TREE_EDITOR = "edit.preferences.group.editor"

	val propertyRendererRegistry = DynamicPropertyRendererRegistry()

	val propertyEditorRegistry = DynamicPropertyEditorRegistry()

	var propertySheetPanelFactory: PropertySheetPanelFactory = PropertySheetPanelFactoryImpl(
		propertyRendererRegistry, propertyEditorRegistry)

	override fun initialize() {
		DrawModuleJvm.require()
		IOModuleJvm.require()

		registerTypes(IOModule.typeMap)

		EditModule.require()

		DrawModuleJvm.contextMenuProvider = EditContextMenuProvider()
		EditModelTextModule.textComponentFactory = TextComponentFactoryJvm()

		configurePropertyRenderer(propertyRendererRegistry)
		configurePropertyEditors(propertyEditorRegistry)

		registerSelectionModels()

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	override fun resetDependencies() {
		DrawModuleJvm.reset()
		IOModuleJvm.reset()
		EditModule.reset()
	}

	@Suppress("UNCHECKED_CAST")
	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.registerRenderer(Direction::class.java, EnumRenderer::class.java)
		registry.registerRenderer(PredefinedColor::class.java, PredefinedColorRenderer::class.java)
		registry.registerRenderer(PredefinedStroke::class.java, PredefinedStrokeRenderer::class.java)
		registry.registerRenderer(Size::class.java, EnumRenderer::class.java)
		registry.registerRenderer(StyleType::class.java, StyleTypeRenderer::class.java)
		registry.registerRenderer(VerticalAlignment::class.java, EnumRenderer::class.java)
		registry.registerRenderer(HorizontalAlignment::class.java, EnumRenderer::class.java)
		registry.registerRenderer(TextProperty::class.java, TextPropertyRenderer::class.java)
		registry.registerRenderer(ScriptProperty::class.java, ScriptPropertyRenderer::class.java)
		registry.register(TranslatableText::class.java) { TranslatablePropertyRenderer((it as CommandPropertySwing<Translatable>).filter) }
		registry.register(Name::class.java) { TranslatablePropertyRenderer() }
		registry.register(Description::class.java) { TranslatablePropertyRenderer((it as CommandPropertySwing<Translatable>).filter) }
		registry.registerRenderer(ImageIdentification::class.java, ImageIdentificationRenderer::class.java)
	}

	@Suppress("UNCHECKED_CAST")
	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(Long::class.java) {
			LongOptionalPropertyEditor(
				isOptional = (it as CommandPropertySwing<Long>).optional
			)
		}
		registry.registerEditor(Direction::class.java, DirectionEditor::class.java)
		registry.registerEditor(Size::class.java, SizeEditor::class.java)
		registry.registerEditor(StyleType::class.java, StyleTypeEditor::class.java)
		registry.registerEditor(VerticalAlignment::class.java, VerticalAlignmentEditor::class.java)
		registry.registerEditor(HorizontalAlignment::class.java, HorizontalAlignmentEditor::class.java)
		registry.register(PredefinedColor::class.java) { PredefinedColorEditor(PredefinedColorRepository) }
		registry.register(PredefinedStroke::class.java) { PredefinedStrokeEditor(PredefinedStrokeRepository) }
		registry.register(TextProperty::class.java) {
			TextPropertyEditor(
				propertyName = (it as CommandPropertySwing<TranslatableText>).displayName)
		}
		registry.register(ScriptProperty::class.java) {
			ScriptPropertyEditor(
				propertyName = (it as ScriptPropertySwing).displayName,
				editable = it.editable,
				helpId = it.helpId,
				parserFactory = it.parserFactory)
		}
		registry.register(TranslatableText::class.java) {
			TranslatablePropertyEditor(
				propertyName = (it as CommandPropertySwing<Translatable>).displayName,
				multiline = it.filter)
		}
		registry.register(Name::class.java) {
			TranslatablePropertyEditor(
				propertyName = (it as CommandPropertySwing<Translatable>).displayName)
		}

		registry.register(Description::class.java) {
			TranslatablePropertyEditor(
				propertyName = (it as CommandPropertySwing<Translatable>).displayName,
				multiline = it.filter)
		}

		registry.registerEditor(ImageIdentification::class.java, ImageIdentificationEditor::class.java)
	}

	private fun registerTypes(typeMap: TypeMap) {
		typeMap.register("text", TextComponent::class)
	}

	private fun registerSelectionModels() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.REPLACE,
			TextComponentJvm::class
		) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent, drawStrategy = RectangularReplaceSelectionModel.DrawStrategy.COMPONENT) }
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
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

		root.getGroup(PREF_TREE_EDITOR).add(EnumPreference(
			Grid.PROP_GRID_PAINTER,
			nameKey = "edit.preferences.Grid.painter",
			values = GridType.values(),
			withName = GridType::withId
		))

		root.getGroup(PREF_TREE_EDITOR).add(IntPreference(
			id = SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT,
			nameKey = "edit.preferences.CommandManager.maxCmdPerSnapshot",
			minValue = 5,
			maxValue = 100
		))
		root.getGroup(PREF_TREE_EDITOR).add(EnumPreference(
			RubberBandHandler.PROP_SELECT_TARGET_STRATEGY,
			nameKey = "edit.preferences.RubberBand.targetStrategy",
			values = RubberBandHandler.SelectionTargetStrategy.values(),
			withName = RubberBandHandler.SelectionTargetStrategy::withName,
			needsRestart = true
		))
	}
}