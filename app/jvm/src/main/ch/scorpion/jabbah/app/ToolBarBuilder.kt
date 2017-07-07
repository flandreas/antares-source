package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent

/**
 * Builds standard [ToolBar]s
 */
object ToolBarBuilder {

    /** Creates a [ToolBar] containing the standard drawing tools.*/
    fun createDrawingToolBar(editor: Editor): ToolBar {
        val toolBar = ToolBar(editor);

        // TODO I18N

        toolBar.addTool(editor.currentTool, "/img/pointer.gif", "Selektion")
        toolBar.addTool(RectangleTool(editor, { RectangularComponent() }), "/img/rectangle.gif", "Rechteck")
        toolBar.addTool(PolylineTool(editor, { PolylineComponent() }), "/img/polyline.gif", "Polyline")

        return toolBar
    }
}