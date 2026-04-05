package io.antarescircuit.jabbah.graph.view.net.netview

import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class NetViewImplTest {

	private val builder: GraphViewBuilder<Boolean>

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder()
	}

	@Test
	fun shouldCombineNetViews() {
		val vvA1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vvA1", 100, 100))
		val vvA2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vvA2", 200, 100))
		val evA = builder.connect(vvA1, vvA2)

		val vvB1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vvB1", 100, 200))
		val vvB2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vvB2", 200, 200))
		val evB = builder.connect(vvB1, vvB2)

		val oldNetViewB = evB.netView

		evA.netView!!.combine(evB.netView!!)

		assertEquals(2, evA.netView!!.size)
		assertEquals(0, oldNetViewB!!.size)
		assertEquals(vvA1.model.getOutput<Boolean>().net, vvA2.model.getInput<Boolean>().net)
		assertEquals(vvA1.model.getOutput<Boolean>().net, vvB1.model.getOutput<Boolean>().net)
		assertEquals(vvA1.model.getOutput<Boolean>().net, vvB2.model.getInput<Boolean>().net)
	}
}