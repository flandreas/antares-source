package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.invocation.SynchronousInvocationHandler
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.view.GraphViewTestRule

object GraphUITestRule {

	fun configure() {
		GraphViewTestRule.configure()

		UiUtil.eventQueueInvoker = {
			false
		}
	}
}