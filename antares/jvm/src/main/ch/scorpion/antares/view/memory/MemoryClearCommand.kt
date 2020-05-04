package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.memory.MemoryDump
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView

/** A [Command] for clearing the contents of a [Memory].*/
class MemoryClearCommand(
	private val application: Application,
	private val addressableId: Int,
	private val bitWidth: BitWidth
) : AbstractCommand("antares.command.clearMemory"), Undoable {

	private val graphView: GraphView get() = (application.data!!.content as MetaGraph).graph.graphView
	private val addressable: Addressable get() = graphView.graph!!.withId(addressableId) as Addressable

	private var oldContents: String? = null

	override fun execute() {
		oldContents = MemoryDump.write(addressable.memory, bitWidth)
		addressable.clear()
	}

	override fun undo() {
		MemoryDump.read(addressable.memory, oldContents!!)
		addressable.update()
	}
}