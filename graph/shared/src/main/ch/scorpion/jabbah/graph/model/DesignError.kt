package ch.scorpion.jabbah.graph.model

/**
 * Represents an error in a [Graph]'s design.
 * @param description a translated, displayable description of the error
 */
data class DesignError(val description: String)