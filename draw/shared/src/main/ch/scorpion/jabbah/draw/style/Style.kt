package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A [Style] is a named set of graphical properties used for drawing graphical objects.
 */
interface Style {
    val color: CompositeColor
    val stroke: Stroke
    val font: Font

    companion object {

        /** The name of the default foreground [Color] in [Properties].*/
        val PROP_FOREGROUND_COLOR = "draw.style.foregroundColor"

        /** The name of the default background [Color] in [Properties].*/
        val PROP_BACKGROUND_COLOR = "draw.style.backgroundColor"

        /** The name of the default text [Color] in [Properties].*/
        val PROP_TEXT_COLOR = "draw.style.textColor"

        /** The name of the default [Stroke] property in [Properties].*/
        val PROP_STROKE = "draw.style.stroke"

        /** The name of the default [Font] property in [Properties].*/
        val PROP_FONT = "draw.style.font"
    }
}

open class BasicStyle(
        override val color: CompositeColor = CompositeColor(
            DrawModule.properties.getColor(Style.PROP_FOREGROUND_COLOR),
            DrawModule.properties.getColor(Style.PROP_BACKGROUND_COLOR),
            DrawModule.properties.getColor(Style.PROP_TEXT_COLOR)),
        override val stroke: Stroke = DrawModule.properties.getStroke(Style.PROP_STROKE),
        override val font: Font = DrawModule.properties.getFont(Style.PROP_FONT)
) : Style