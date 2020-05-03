package ch.scorpion.jabbah.edit.model.text

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
 * A [Tool] for adding a new [TextComponent] to a [Drawing].
 */
class TextTool(
	editor: Editor,
	service: DrawingAppService = EditModule.drawingAppService,
	factory: () -> TextComponent,
	adder: (TextComponent) -> Component = { it }
) : AbstractComponentTool<TextComponent>(editor, service, factory, adder) {

    override fun activate() {
        editor.view.setCursor(Cursor.TEXT)
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