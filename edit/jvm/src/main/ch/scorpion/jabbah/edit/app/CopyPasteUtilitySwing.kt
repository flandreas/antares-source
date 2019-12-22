package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.TypeMap

object CopyPasteUtilitySwing : CopyPasteUtility {

	private val LOG by logger(CopyPasteUtilitySwing::class)

	override fun cut(
		view: DrawingView<Drawing<Component>>,
		components: Collection<Component>,
		typeMap: TypeMap,
		commandManager: CommandManager
	) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun copy(drawing: Drawing<*>, components: Collection<Component>, typeMap: TypeMap) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun paste(
		view: DrawingView<Drawing<Component>>,
		storableCreator: StorableCreator,
		typeMap: TypeMap,
		commandManager: CommandManager
	) {
		throw UnsupportedOperationException("not implemented")
	}
}