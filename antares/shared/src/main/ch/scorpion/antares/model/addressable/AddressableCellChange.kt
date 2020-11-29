package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView

/** Represents the change of the value of an [Addressable] cell by the user.*/
data class AddressableCellChange(val address: Int, val newValue: Long, val origValue: Long)

class AddressableCellChangeCommand(
	private val controller: ApplicationDataViewController,
	private val addressableId: Int,
	private val changes: Collection<AddressableCellChange>
) : AbstractCommand("antares.command.memoryContents", null), Undoable {

	private val graphView: GraphView get() = (controller.data!!.content as MetaGraph).graph.graphView
	private val addressable: Addressable get() = graphView.graph!!.withId(addressableId) as Addressable

	override fun execute() {
		changes.forEach { addressable.memory.write(it.address, it.newValue) }
	}

	override fun undo() {
		changes.forEach { addressable.memory.write(it.address, it.origValue) }
	}
}