package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.app.ComponentSnapAction
import ch.scorpion.jabbah.edit.app.GridSnapAction
import ch.scorpion.jabbah.edit.model.curve.CubicCurveComponent
import ch.scorpion.jabbah.edit.model.curve.CubicCurveTool
import ch.scorpion.jabbah.edit.model.curve.QuadCurveTool
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.text.ComponentAtLocationTool
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import javax.swing.JToggleButton

open class ContainerToolBarBuilder {

	fun buildToolBars(
		application: Application?,
		editor: ContainerEditor,
		separator: Boolean = false,
	): MutableList<ToolBar> {
		val toolBars = mutableListOf(
			buildDrawingToolsToolbar(application, editor, separator),
			buildSettingsToolbar(editor)
		)

		toolBars.forEach { it.isFloatable = false }
		return toolBars
	}

	open fun buildDrawingToolsToolbar(application: Application?, editor: ContainerEditor, separator: Boolean): ToolBar {
		val toolbar = ToolBar(editor)
		if (separator) {
			toolbar.addSeparator()
		}

		if (application != null) {
			toolbar.addAction(application.controller.saveAction)
			toolbar.addGap()
		}

		toolbar.addTool(editor.selectionTool, "/img/pointer24.png", Translations.getString("edit.tool.select"))
		toolbar.addTool(ComponentAtLocationTool(editor, cursor = Cursor.TEXT, factory = { LabelComponent() } ), "/img/text24.png", Translations.getString("edit.component.label"))
		toolbar.addTool(RectangleTool(editor, factory = { RectangleComponent() }), "/img/rectangle24.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(RectangleTool(editor, factory = { EllipseComponent() }), "/img/oval24.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(PolylineTool(editor, factory = { PolylineComponent() }), "/img/polyline24.png", Translations.getString("edit.component.polyline"))
		toolbar.addTool(QuadCurveTool(editor, factory = { QuadCurveComponent() }), "/img/curve24.png", Translations.getString("edit.component.quadraticCurve"))
		// TODO Icon Janis
		toolbar.addTool(CubicCurveTool(editor, factory = { CubicCurveComponent() }), "/img/curve24.png", Translations.getString("edit.component.cubicCurve"))

		return toolbar
	}

	private fun buildSettingsToolbar(editor: ContainerEditor): ToolBar {
		val toolBar = ToolBar(editor)
		toolBar.addSeparator()

		val gridButton = JToggleButton(ActionWrapperSwing(GridSnapAction(editor)))
		gridButton.text = null
		gridButton.isFocusPainted = false
		gridButton.icon = UiUtil.themedIcon("/img/grid24.png")
		gridButton.toolTipText = Translations.getString("edit.action.grid.snap.name")
		toolBar.add(gridButton)

		val button = JToggleButton(ActionWrapperSwing(ComponentSnapAction(editor)))
		button.text = null
		button.isFocusPainted = false
		button.icon = UiUtil.themedIcon("/img/snap24.png")
		button.toolTipText = Translations.getString("edit.tool.align.name")
		toolBar.add(button)

		return toolBar
	}
}