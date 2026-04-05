package io.antarescircuit.jabbah.edit.drag

import io.antarescircuit.jabbah.base.AbstractModule

object EditDragModule : AbstractModule() {

	val dragDestinationHighlightFactoryRegistry = DragDestinationHighlightFactoryRegistry()

	override fun initialize() { }

	override fun resetDependencies() { }
}