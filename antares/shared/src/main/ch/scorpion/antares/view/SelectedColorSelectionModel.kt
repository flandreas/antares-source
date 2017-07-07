package ch.scorpion.antares.view

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.style.Themes


/**
 * A [SelectionModel] that renders the [Component] in the selection color [Theme.selection].
 * Used for [SelectionDrawingStrategy.REPLACE].
 */
class SelectedColorSelectionModel<T : Component>(component: T) : AbstractSelectionModel<T>(component){

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        val oldUseContextColors = context.useContextColors

        context.g.color = Themes.get<AntaresTheme>().selection.foregroundColor
        context.useContextColors = true
        context.color = Themes.get<AntaresTheme>().selection

        component.draw(context)
        context.g.color = oldColor
        context.useContextColors = oldUseContextColors
    }

    override val boundingBox: RectangularShape
        get() = component.boundingBox

    override fun contains(x: Double, y: Double): Boolean {
        return component.contains(x, y)
    }

    override fun componentUpdated() {
        validate()
    }
}