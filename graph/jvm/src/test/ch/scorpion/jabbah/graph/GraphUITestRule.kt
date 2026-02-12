package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

object GraphUITestRule {

	fun configure() {
		GraphViewTestRule.configure()

		UiUtil.eventQueueInvoker = {
			false
		}
	}
}