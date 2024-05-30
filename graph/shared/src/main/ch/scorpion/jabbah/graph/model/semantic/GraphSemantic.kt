package ch.scorpion.jabbah.graph.model.semantic

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.semantic.Semantic

/**
 * Enumerates all [Semantic]s used in the graph module.
 */
enum class GraphSemantic(
    override val customName: String,
    private val translationKey: String
) : Semantic {

    PropagationDelay("propagationDelay", "element.property.propagationDelay.name");

    override fun toString(): String = customName

    override val translatedName: String get() = Translations.getString(translationKey)
}