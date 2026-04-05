package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewEditingTest
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class EdgeViewSelectBuddyTest : AbstractGraphViewEditingTest() {

	private lateinit var vv1: TestVerticeView
	private lateinit var vv2: TestVerticeView
	private lateinit var vv3: TestVerticeView
	private lateinit var ev: EdgeView<Boolean>

	override fun setupCircuit() {
		vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 100, 100))
		vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 200, 100))
		vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 200, 200))
		ev = builder.connect(vv1, vv2)
		builder.split(ev, 0, Point2D(150, 100), vv3)
	}

	@Test
	fun shouldSelectSingleNetViewElement() {
		driver.mouseMoveTo(130, 100)
		driver.pressMouseAt(130, 100)
		driver.releaseMouseAt(130, 100)

		assertEquals(1, editor.view.selectionManager.selectionCount)
	}

	@Test
	fun shouldSelectAllNetViewElements() {
		driver.mouseMoveTo(130, 100)
		driver.pressMouseAt(130, 100, Modifier.Meta.mask)
		driver.releaseMouseAt(130, 100)

		assertEquals(3, editor.view.selectionManager.selectionCount)
	}
}