package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.VerticeView
/**
 * Determines the position of a label in relation to the [VerticeView].
 * Although this is quite the same as [PortLabelPosition], it might evolve to more enumeration values
 * like "above" or "below", which is not possible with [PortLabelPosition].
 */
enum class VerticeLabelPosition(val customName: String) {

    HIDE("hide"),
    INTERNAL("internal"),
    EXTERNAL("external");

    companion object {

        fun withName(customName: String): VerticeLabelPosition {
            for (pos in values()) {
                if (pos.customName == customName) {
                    return pos
                }
            }
            throw IllegalArgumentException("unknown VerticeLabelPosition $customName")
        }
    }

    override fun toString(): String {
        return when(this) {
            HIDE -> Translations.getString("graph.property.VerticeLabelPosition.hide.name")
            EXTERNAL -> Translations.getString("graph.property.VerticeLabelPosition.external.name")
            INTERNAL -> Translations.getString("graph.property.VerticeLabelPosition.internal.name")
        }
    }
}