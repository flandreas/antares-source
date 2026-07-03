package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

enum class OscilloscopeSignalCurveStyle(override val customName: String) : EnumProperty<OscilloscopeSignalCurveStyle> {

    RECTANGULAR("rectangular"),

    DIAGONAL("diagonal");

    companion object {
        fun withCustomName(customName: String): OscilloscopeSignalCurveStyle =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Unknown OscilloscopeSignalCurveStyle: $customName")
    }

    override fun toString(): String {
        return when (this) {
            RECTANGULAR -> Translations.getString("graph.property.oscilloscope.signalCurveStyle.rectangular")
            DIAGONAL -> Translations.getString("graph.property.oscilloscope.signalCurveStyle.diagonal")
        }
    }
}