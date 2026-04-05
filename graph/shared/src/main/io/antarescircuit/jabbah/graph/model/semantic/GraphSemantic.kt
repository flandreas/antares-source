package io.antarescircuit.jabbah.graph.model.semantic

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.semantic.Semantic

/**
 * Enumerates all [Semantic]s used in the graph module.
 */
enum class GraphSemantic(
    override val customName: String,
    private val translationKey: String
) : Semantic {

    PropagationDelay("propagationDelay", "graph.property.propagationDelay.name");

    override fun toString(): String = customName

    override val translatedName: String get() = Translations.getString(translationKey)
}