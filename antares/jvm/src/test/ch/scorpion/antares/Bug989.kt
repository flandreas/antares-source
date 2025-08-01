package ch.scorpion.antares

import ch.scorpion.antares.view.AbstractGraphViewEditingTest
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Bug989 : AbstractGraphViewEditingTest(100) {

    private val graphView: GraphView get() = editor.view.drawing as GraphView

    @BeforeTest
    fun setUp() {
        AntaresTestRule.configure()
        EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
    }

    override fun setupCircuit() {
        // Cannot build circuit because CommandManager is not yet bound.
        editor.selectionTool.rubberBandHandler.delaySelectTimer = null
    }

    @Test
    fun testJoinLeftToRight() {
        play(true)
    }

    @Test
    fun testJoinRightToLeft() {
        play(false)
    }

    private fun play(joinLeftToRight: Boolean) {
        var switchView1 = SwitchView().apply { location = Point2D(0, 0) }
        switchView1 = EditModule.drawingAppService.add(switchView1, editor.view) as SwitchView

        var triStateBufferView = TriStateBufferGateView().apply { location = Point2D(200, 0) }
        triStateBufferView = EditModule.drawingAppService.add(triStateBufferView, editor.view) as TriStateBufferGateView

        var switchView2 = SwitchView().apply { location = Point2D(0, 200) }
        switchView2 = EditModule.drawingAppService.add(switchView2, editor.view) as SwitchView

        var ledView = LEDView().apply { location = Point2D(500, 200) }
        ledView = EditModule.drawingAppService.add(ledView, editor.view) as LEDView

        // Connect SwitchView1 with TriStateBuffer input
        driver.mouseMoveTo(0, 0)
        driver.pressMouseAt(0, 0)
        driver.dragMouseTo(200, 0)
        driver.releaseMouseAt(200, 0)
        assertEquals(1, graphView.getEdgeViews().size)

        // Connect SwitchView2 with LedView
        driver.mouseMoveTo(0, 200)
        driver.pressMouseAt(0, 200)
        driver.dragMouseTo(500, 200)
        driver.releaseMouseAt(500, 200)
        assertEquals(2, graphView.getEdgeViews().size)

        // Connect TriStateBuffer output with EdgeView SwitchView2->LedView
        driver.mouseMoveTo(270, 0)
        driver.pressMouseAt(270, 0)
        driver.dragMouseTo(400, 200)
        driver.releaseMouseAt(400, 200)
        assertEquals(4, graphView.getEdgeViews().size)

        // Delete TriStateBuffer to get two open-ended EdgeViews
        EditModule.drawingAppService.delete(listOf(triStateBufferView), editor.view)

        // Join open-ended EdgeViews
        if (joinLeftToRight) {
            driver.mouseMoveTo(200, 0)
            driver.pressMouseAt(200, 0)
            driver.dragMouseTo(270, 0)
            driver.releaseMouseAt(270, 0)
        } else {
            driver.mouseMoveTo(270, 0)
            driver.pressMouseAt(270, 0)
            driver.dragMouseTo(200, 0)
            driver.releaseMouseAt(200, 0)
        }

        assertEquals(3, graphView.getEdgeViews().size)
        assertEquals(1, graphView.netViewsCount)

        // Find EdgeView connected with LedView
        val ev = graphView.getEdgeViews()
            .first { it.model.isConnectedWith(ledView.model.getInput()) }

        // Delete EdgeView connected with LedView
        EditModule.drawingAppService.delete(listOf(ev), editor.view)

        assertEquals(1, graphView.getEdgeViews().size)
        assertEquals(4, graphView.drawables.size) // 3 VV, 1 EV
        assertEquals(4, graphView.graph!!.elementsCount)
        assertEquals(1, graphView.netViewsCount)

        assertNull(GraphViewConsistencyCheck.execute(graphView))
    }
}