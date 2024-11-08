package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

object GraphModule : AbstractModule() {

	private val DUMMY_APP_CONTEXT = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()))

	override fun initialize() {
		GraphViewModule.require()

		DrawModule.drawContextFactory = { g, mc, appContext ->
			if (appContext == null) {
				DrawContext(g, mc, DUMMY_APP_CONTEXT)
			} else {
				DrawContext(g, mc, appContext)
			}
		}
	}
}