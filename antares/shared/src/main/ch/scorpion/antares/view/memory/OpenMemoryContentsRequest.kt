package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.event.InputEvent
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Represents a request to open and display the contents of a [Memory].
 */
data class OpenMemoryContentsRequest(
	val verticeView: VerticeView<*>,
	val name: String,
    val memory: Memory,
	val addressable: Addressable,
	val readonly: Boolean,
	val newDesktopView: Boolean = false
)