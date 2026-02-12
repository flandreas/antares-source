package ch.scorpion.jabbah.edit.drag

import ch.scorpion.jabbah.base.AbstractModule

object EditDragModule : AbstractModule() {

	val dragDestinationHighlightFactoryRegistry = DragDestinationHighlightFactoryRegistry()

	override fun initialize() { }

	override fun resetDependencies() { }
}