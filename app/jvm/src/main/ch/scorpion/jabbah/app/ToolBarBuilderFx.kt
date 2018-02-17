package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.ActionWrapperFx
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.app.ComponentSnapAction
import ch.scorpion.jabbah.edit.app.ToolAction
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.model.text.TextTool
import javafx.scene.control.Separator
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToolBar

class ToolBarBuilderFx(private val editor: Editor) {

	fun build(): ToolBar {
		val toolbar = ToolBar()

		toolbar.items.add(ActionWrapperFx.imageButton(ToggleButton(),
			ToolAction("edit.tool.select", editor.currentTool, editor, "/img/pointer24.png")))
		toolbar.items.add(ActionWrapperFx.imageButton(ToggleButton(),
			ToolAction("edit.tool.rectangle", RectangleTool(editor, { RectangleComponent() } ), editor, "/img/rectangle24.png")))
		toolbar.items.add(ActionWrapperFx.imageButton(ToggleButton(),
			ToolAction("edit.tool.ellipse", RectangleTool(editor, { EllipseComponent() } ), editor, "/img/oval24.png")))
		toolbar.items.add(ActionWrapperFx.imageButton(ToggleButton(),
			ToolAction("edit.tool.polyline", PolylineTool(editor, { PolylineComponent() } ), editor, "/img/polyline24.png")))
		toolbar.items.add(ActionWrapperFx.imageButton(ToggleButton(),
			ToolAction("edit.tool.text", TextTool(editor, { TextComponentJvm("Text") } ), editor, "/img/text24.png")))

		toolbar.items.add(Separator())

		val snapAction = ComponentSnapAction(editor)
		snapAction.imagePath = "/img/align24.png"
		toolbar.items.add(ActionWrapperFx.imageButton(ToggleButton(), snapAction))

		return toolbar
	}
}