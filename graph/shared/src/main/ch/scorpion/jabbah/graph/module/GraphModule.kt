package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.CombinedMetaGraphRepository
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

object GraphModule : AbstractModule() {

	val metaGraphRepository = CombinedMetaGraphRepository()

	override fun initialize() {
		GraphViewModule.require()
	}
}