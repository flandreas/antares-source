package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Enumerates the colors of [VerticeView]s that emit light.
 */
enum class LightColor(val customName: String, val onColor: Color, val offColor: Color) {

    RED("red", Color(255, 0, 0), Color(60, 0, 0)),
    YELLOW("yellow", Color(255, 255, 0), Color(60, 47, 0)),
    GREEN("green", Color(0, 255, 0), Color(0, 60, 0)),
    BLUE("blue", Color(0, 255, 255), Color(0, 0, 60));

    companion object {

        fun withName(customName: String): LightColor {
            for (c in LightColor.values()) {
                if (c.customName == customName) {
                    return c
                }
            }
            throw IllegalArgumentException("Unknown LightColor '$customName'")
        }
    }

    override fun toString(): String {
        return when(this) {
            RED -> Translations.getString("element.color.red")
            YELLOW -> Translations.getString("element.color.yellow")
            GREEN -> Translations.getString("element.color.green")
            BLUE -> Translations.getString("element.color.blue")
        }
    }
}