package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for adding a [Component] to a [Drawing].
 */
class AddCommand(
    private val drawingView: DrawingView<Drawing<in Component>>,
    val component: Component
) : AbstractCommand("edit.command.add", null) {

    constructor(editor: Editor, component: Component): this(editor.view, component)

    override fun execute() {
        drawingView.drawing.add(component)
    }

    override fun undo() {
        drawingView.drawing.remove(component)
    }
}