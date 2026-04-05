package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
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
 * A [Tool] for adding a new [TextComponent] to a [Drawing].
 */
class TextTool(
	editor: Editor,
	service: DrawingAppService = EditModule.drawingAppService,
	factory: () -> TextComponent,
	adder: (TextComponent) -> Component = { it }
) : AbstractComponentTool<TextComponent>(editor, service, factory, adder) {

    override fun activate() {
	    super.activate()
        editor.view.setCursor(Cursor.TEXT)
	    Status.set(StatusType.Tool, "Click to enter text")
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        super.mousePressed(e, x, y)

        val instance: TextComponent = createComponent()

        // Snap mouse pressed location
        val offset = editor.snapManager.snap(x, y)
        instance.location = Point2D(x + offset.x, y + offset.y)

	    addComponent( getAddedComponent(instance))

        editor.toolDone()
    }
}