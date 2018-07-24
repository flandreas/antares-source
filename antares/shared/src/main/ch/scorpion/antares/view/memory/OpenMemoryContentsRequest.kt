package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.event.InputEvent

/**
 * Represents a request to open and display the contents of a [Memory].
 */
data class OpenMemoryContentsRequest(
	val name: String,
    val memory: Memory,
    val addressWidth: BitWidth,
    val dataWidth: BitWidth,
    val inputEvent: InputEvent
)