package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.library.LibraryModule

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