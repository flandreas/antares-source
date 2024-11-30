package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.library.LibraryModule

data class MemoryStorableIdentification(
    val uuid: UUID,
    val name: Name
) {
    companion object {
        fun getAll(): List<MemoryStorableIdentification> =
            LibraryModule.libraryHolder.library
                .allLocalItems { it is MemoryLibraryItem }
                .map { it as MemoryLibraryItem }
                .map { MemoryStorableIdentification(it.uuid, it.name) }
    }

    override fun toString(): String = name.value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MemoryStorableIdentification

        return uuid == other.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}