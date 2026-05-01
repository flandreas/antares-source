package io.antarescircuit.jabbah.edit.command

import io.antarescircuit.jabbah.edit.*

/**
 * Implemented by [Commands][Command] that don't have an [Editor] and therefore
 * must override [Command.validate] by using [DrawingView] to validate the [Drawing].
 */
abstract class AbstractDrawingViewCommand(
    descriptionKey: String,
    protected val view: DrawingView<*,*>
) : AbstractCommand(descriptionKey) {

    override fun validate() {
        view.drawing.validate()
    }
}