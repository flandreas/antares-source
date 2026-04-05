package io.antarescircuit.antares.model

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.model.OutputPort

/**
 * Represents the supported annotation to be used for digital [OutputPort]s
 */
enum class OutputAnnotation(val customName: String) {
    NONE("none"),
    TRI_STATE("triState"),
    MASTER_SLAVE("masterSlave");

    companion object {
        fun withName(customName: String): OutputAnnotation {
            for (outputAnnotation in values()) {
                if (outputAnnotation.customName == customName) {
                    return outputAnnotation
                }
            }
            throw IllegalArgumentException("unknown OutputAnnotation $customName")
        }
    }

    override fun toString(): String {
        return when(this) {
            NONE -> Translations.getString("element.property.OutputAnnotation.none")
            TRI_STATE -> Translations.getString("element.property.OutputAnnotation.triState")
            MASTER_SLAVE -> Translations.getString("element.property.OutputAnnotation.masterSlave")
        }
    }
}