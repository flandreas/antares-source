package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView

/** Represents the change of the value of a memory cell by the user.*/
data class MemoryCellChange(val address: Int, val newValue: Long, val origValue: Long)

class MemoryCellChangeCommand(
	private val application: Application,
	private val addressableId: Int,
	private val changes: Collection<MemoryCellChange>
) : AbstractCommand("antares.command.memoryContents", null), Undoable {

	private val graphView: GraphView get() = (application.data!!.content as MetaGraph).graph.graphView
	private val addressable: Addressable get() = graphView.graph!!.withId(addressableId) as Addressable

	override fun execute() {
		changes.forEach { addressable.memory.write(it.address, it.newValue) }
	}

	override fun undo() {
		changes.forEach { addressable.memory.write(it.address, it.origValue) }
	}
}