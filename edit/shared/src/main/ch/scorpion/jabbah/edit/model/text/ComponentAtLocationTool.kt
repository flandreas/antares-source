package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.model.AbstractComponentTool
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * A [Tool] for interactively creating a [Component] in a [Drawing] at the mouse click location.
 */
class ComponentAtLocationTool(
	editor: Editor,
	service: DrawingAppService = EditModule.drawingAppService,
	factory: () -> Component,
	private val cursor: Cursor = Cursor.CROSSHAIR,
	adder: (Component) -> Component = { it }
) : AbstractComponentTool<Component>(editor, service, factory, adder) {

    override fun activate() {
        editor.view.setCursor(cursor)
	    Status.set(StatusType.Tool, Translations.getString("edit.tool.addComponent.text"))
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        super.mousePressed(e, x, y)

	    val component = createComponent()

        // snap the mouse pressed location
        val offset = editor.snapManager.snap(x, y)
        component.location = Point2D(x + offset.x, y + offset.y)

	    addComponent(getAddedComponent(component))

        editor.toolDone()
    }
}