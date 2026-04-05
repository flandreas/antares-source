package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Tool
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.model.AbstractComponentTool
import io.antarescircuit.jabbah.edit.module.EditModule

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