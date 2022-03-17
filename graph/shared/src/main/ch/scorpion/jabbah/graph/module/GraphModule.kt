package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.CombinedMetaGraphRepository
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

object GraphModule : AbstractModule() {

	val metaGraphRepository = CombinedMetaGraphRepository()

	override fun initialize() {
		GraphViewModule.require()

		DrawModule.drawContextFactory = { g, mc, appContext ->
			if (appContext == null) {
				DrawContext(g, mc, GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed())))
			} else {
				DrawContext(g, mc, appContext)
			}
		}
	}
}