package io.antarescircuit.antares.view.analog

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.analog.AnalogNet
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Graphics2D
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrentFlowVisualizationTest {

    private val systemSpeed = SystemSpeed()
    private val graphics: Graphics2D = mock(MockMode.autofill)
    private val appContext = GraphApplicationContext(CurrentSystemSpeedCategory(systemSpeed))
    private val drawContext: DrawContext = DrawContext(graphics, appContext = appContext)
    private val x = Capture.slot<Double>()
    private val y = Capture.slot<Double>()

    private val points = mutableListOf<Point2D>()

    private lateinit var edgeView: AnalogEdgeView

    @BeforeTest
    fun setup() {
        AntaresTestRule.configure()
        every { graphics.color } returns Color.RED
        every { graphics.stroke } returns Stroke()
        every { graphics.fillRect(capture(x), capture(y), any(), any()) } calls {
            points.add(Point2D(x.get() + CurrentFlowVisualization.HALF_SIZE, y.get() + CurrentFlowVisualization.HALF_SIZE))
        }

        edgeView = AnalogEdgeView()
            .addSegmentPoint(Point2D.ZERO)
            .addSegmentPoint(Point2D(100, 0))
                as AnalogEdgeView

        (edgeView.net as AnalogNet).setNode(edgeView.id, 0, 0)
    }

    @Test
    fun shouldDrawInInitialPosition() {
        CurrentFlowVisualization.draw(edgeView, drawContext)

        assertEquals(Point2D(0, 0), points.first())
        assertEquals(Point2D(90, 0), points.last())
    }

    @Test
    fun shouldDrawCycle() {
        edgeView.current = 0.05 // 50 mA
        CurrentFlowAnimationSpeed.speed = CurrentFlowAnimationSpeed.MAX_SPEED

        tickAndExpectX(2.0)
        tickAndExpectX(4.0)
        tickAndExpectX(6.0)
        tickAndExpectX(8.0)
        tickAndExpectX(10.0)
        tickAndExpectX(12.0)
        tickAndExpectX(14.0)

        // Cycle is limited by distance 15
        tickAndExpectX(1.0)
    }

    @Test
    fun shouldReverse() {
        edgeView.current = 0.05 // 50 mA
        CurrentFlowAnimationSpeed.speed = CurrentFlowAnimationSpeed.MAX_SPEED

        tickAndExpectX(2.0)
        tickAndExpectX(4.0)
        tickAndExpectX(6.0)
        tickAndExpectX(8.0)
        tickAndExpectX(10.0)

        edgeView.current = -0.05

        tickAndExpectX(8.0)
        tickAndExpectX(6.0)
        tickAndExpectX(4.0)
        tickAndExpectX(2.0)
        tickAndExpectX(0.0)

        tickAndExpectX(13.0)
    }

    private fun tickAndExpectX(x: Double) {
        points.clear()
        edgeView.currentFlowAnimationTick(systemSpeed)
        CurrentFlowVisualization.draw(edgeView, drawContext)
        assertEquals(x, edgeView.animationOffset)
        assertEquals(Point2D(x, 0.0), points.first())
    }
}