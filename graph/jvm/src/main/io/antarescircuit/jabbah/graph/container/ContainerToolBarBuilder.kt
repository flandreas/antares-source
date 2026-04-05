package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ToolBar
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.app.ComponentSnapAction
import io.antarescircuit.jabbah.edit.app.GridSnapAction
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveComponent
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveTool
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveTool
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineTool
import io.antarescircuit.jabbah.edit.model.rectangle.EllipseComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleTool
import io.antarescircuit.jabbah.edit.model.text.ComponentAtLocationTool
import io.antarescircuit.jabbah.edit.model.text.LabelComponent
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
		toolbar.addTool(CubicCurveTool(editor, factory = { CubicCurveComponent() }), "/img/cubic-curve.png", Translations.getString("edit.component.cubicCurve"))

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