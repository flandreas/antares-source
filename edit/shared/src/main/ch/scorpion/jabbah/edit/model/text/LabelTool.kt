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
 * A [Tool] for interactively creating a [LabelComponent] in a [Drawing].
 */
class LabelTool(
	editor: Editor,
	service: DrawingAppService = EditModule.drawingAppService,
	factory: () -> LabelComponent,
	adder: (TextComponent) -> Component = { it }
) : AbstractComponentTool<LabelComponent>(editor, service, factory, adder) {

    override fun activate() {
        editor.view.setCursor(Cursor.TEXT)
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        super.mousePressed(e, x, y)

        //val label = factory.invoke()
	    val label = createComponent()

        // snap the mouse pressed location
        val offset = editor.snapManager.snap(x, y)
        label.location = Point2D(x + offset.x, y + offset.y)

	    addComponent(getAddedComponent(label))

        editor.toolDone()
    }
}