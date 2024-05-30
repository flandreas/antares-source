package ch.scorpion.jabbah.edit.semantic

/**
 * Used for registering and accessing system-wide [Semantic]s.
 */
object SemanticRegistry {

    private val registeredSemantics = mutableSetOf<Semantic>()

    /** Returns all registered [Semantic]s. */
    val semantics: Set<Semantic> get() = registeredSemantics

    fun register(semantic: Semantic) {
        if (registeredSemantics.contains(semantic)) {
            throw IllegalArgumentException("Semantic ${semantic.customName} already registered")
        }
        registeredSemantics.add(semantic)
    }

    fun withCustomName(customName: String): Semantic =
        registeredSemantics.firstOrNull { it.customName == customName }
            ?: throw IllegalArgumentException("Semantic $customName not registered")
}