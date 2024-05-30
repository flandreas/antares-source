package ch.scorpion.jabbah.edit.semantic

/**
 * Defines the structure of objects representing the semantic, i.e. a special meaning
 * that can be given to other, typically configurable objects.
 */
interface Semantic {

    /** The name used for system-wide unique identification, and also used for persistence.*/
    val customName: String

    /** The name translated to the user's language used for identifying it in the UI.*/
    val translatedName: String
}