package io.antarescircuit.jabbah.graph.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

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

	override fun resetDependencies() {
		GraphViewModule.reset()
	}
}