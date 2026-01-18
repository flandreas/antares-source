package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier.Alt
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.graphics.Graphics2DMockBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * In GitHub bug #1119, [SimpleEdgeViewAdjustmentView]'s model produced an [IndexOutOfBoundsException] when
 * connecting EdgeView-to-EdgeView and moving the destination endpoint led to a relayout that joined
 * [EdgeView] segments, which made the [EdgeViewAdjustmentModel] invalid due to indices not existing
 * in the [EdgeView] anymore.
 */
class EdgeViewAdjustmentTest : AbstractInputEventHandlerTest() {

    private lateinit var v3: TestVerticeView
    private lateinit var v4: TestVerticeView

    private val drawContext = DrawContext(Graphics2DMockBuilder().build())

    @BeforeTest
    fun initialize() {
        handler = GraphViewModule.edgeToPortOrEdgeConnector.handler
        v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 100, 200))
        v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 200, 200))

        builder.connect(v1, v2)
        builder.connect(v3, v4)
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun test() {
        mouseMoveTo(150, 200, modifiers = Alt.mask)
        clickMouseAt(150, 200, modifiers = Alt.mask)
        mouseMoveTo(170, 150)
        clickMouseAt(170, 150)
        mouseMoveTo(170, 100)

        // This joined the two last EdgeView segments, making the point indices in EdgeViewAdjustment invalid,
        // and leading to an IndexOutOfBoundsException.
        mouseMoveTo(150, 100)

        GraphViewModule.edgeToPortOrEdgeConnector.adjustment!!.draw(drawContext)
    }
}