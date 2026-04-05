package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

object GraphUITestRule {

	fun configure() {
		GraphViewTestRule.configure()

		UiUtil.eventQueueInvoker = {
			false
		}
	}
}