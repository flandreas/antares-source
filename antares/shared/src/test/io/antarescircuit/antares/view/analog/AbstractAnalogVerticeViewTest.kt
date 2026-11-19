package io.antarescircuit.antares.view.analog

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.analog.Resistor
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Graphics2D
import io.antarescircuit.jabbah.draw.graphics.LinearColorGradient
import io.antarescircuit.jabbah.draw.graphics.PredefinedColorIdentity
import io.antarescircuit.jabbah.draw.graphics.PredefinedColorRepository
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.model.Net
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

class AbstractAnalogVerticeViewTest {

	private lateinit var view: TestView
	private lateinit var context: DrawContext
	private var signal1: AnalogSignal? = AnalogSignal(1.0)
	private var signal2: AnalogSignal? = AnalogSignal(4.0)

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		view = TestView()
		val net1 = mock<Net<AnalogSignal>>(MockMode.autofill)
		val net2 = mock<Net<AnalogSignal>>(MockMode.autofill)
		every { net1.signal } calls { signal1 }
		every { net2.signal } calls { signal2 }
		view.model.getPort<AnalogSignal>(1).connectTo(net1)
		view.model.getPort<AnalogSignal>(2).connectTo(net2)
		context = createContext(ApplicationMode.EXECUTE)
	}

	@Test
	fun shouldReuseGradientWhenVoltagesStayInTheSamePaletteBuckets() {
		view.transparency = 128
		val gradient = view.gradient(context)
		assertSame(gradient, view.gradient(context))

		signal1 = AnalogSignal(1.0001)
		signal2 = AnalogSignal(4.0001)

		assertSame(gradient, view.gradient(context))
	}

	@Test
	fun shouldReplaceGradientWhenEitherColorChanges() {
		val initial = view.gradient(context)
		signal1 = AnalogSignal(2.0)
		val changedFirst = view.gradient(context)
		assertNotSame(initial, changedFirst)
		assertEquals(signal1!!.color.foregroundColor, changedFirst.color2)

		signal2 = AnalogSignal(-3.0)
		val changedSecond = view.gradient(context)
		assertNotSame(changedFirst, changedSecond)
		assertEquals(signal2!!.color.foregroundColor, changedSecond.color1)
		assertSame(changedSecond, view.gradient(context))
	}

	@Test
	fun shouldReplaceGradientWhenTransparencyChanges() {
		val opaque = view.gradient(context)
		view.transparency = 128
		val translucent = view.gradient(context)

		assertNotSame(opaque, translucent)
		assertEquals(opaque.color1.withAlpha(128), translucent.color1)
		assertEquals(opaque.color2.withAlpha(128), translucent.color2)
		assertSame(translucent, view.gradient(context))
	}

	@Test
	fun shouldReplaceGradientWhenEndpointCoordinatesChange() {
		var previous = view.gradient(context)
		val frames = listOf(
			Rectangle2D(-10, 0, 110, 20), // Left endpoint only
			Rectangle2D(-10, 0, 120, 20), // Right endpoint only
			Rectangle2D(-10, 0, 120, 40), // Center Y only
		)
		for (frame in frames) {
			view.bounds.setFrame(frame.x, frame.y, frame.width, frame.height)
			val gradient = view.gradient(context)
			assertNotSame(previous, gradient)
			assertEquals(view.bounds.centerLeft, gradient.p1)
			assertEquals(view.bounds.centerRight, gradient.p2)
			assertSame(gradient, view.gradient(context))
			previous = gradient
		}
	}

	@Test
	fun shouldRespectReversedPortOrder() {
		val normal = view.gradient(context)
		val reversed = view.gradient(context, 2, 1)

		assertNotSame(normal, reversed)
		assertEquals(normal.color1, reversed.color2)
		assertEquals(normal.color2, reversed.color1)
		assertSame(reversed, view.gradient(context, 2, 1))
	}

	@Test
	fun shouldUpdateFallbackColorsAfterDisconnectingAndChangingCustomColor() {
		val connected = view.gradient(context)
		view.model.getPort<AnalogSignal>(1).disconnect()
		view.model.getPort<AnalogSignal>(2).disconnect()
		val disconnected = view.gradient(context)
		assertNotSame(connected, disconnected)
		assertEquals(view.foregroundColor, disconnected.color1)
		assertEquals(view.foregroundColor, disconnected.color2)

		view.customColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Red)
		val custom = view.gradient(context)
		assertNotSame(disconnected, custom)
		assertEquals(view.foregroundColor, custom.color1)
		assertEquals(view.foregroundColor, custom.color2)
	}

	@Test
	fun shouldReturnNullWhenNetStateIsHiddenEvenAfterCaching() {
		val gradient = view.gradient(context)

		assertNull(view.optionalGradient(createContext(ApplicationMode.EDIT)))
		assertSame(gradient, view.gradient(context))
	}

	@Test
	fun shouldKeepCachesSeparateBetweenViews() {
		val other = TestView()
		view.model.getPort<AnalogSignal>(1).disconnect()
		view.model.getPort<AnalogSignal>(2).disconnect()

		assertNotSame(view.gradient(context), other.gradient(context))
	}

	private fun createContext(mode: ApplicationMode): DrawContext = DrawContext(
		mock<Graphics2D>(MockMode.autofill),
		appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()), mode, isPausing = true),
	)

	private class TestView : AbstractAnalogVerticeView<Resistor>(
		model = Resistor(),
		externalLabelDirection = Direction.NORTH,
		bounds = Rectangle2D(0, 0, 100, 20),
	) {
		override val relativeExternalLabelLocation: Point2D get() = Point2D.ZERO

		fun gradient(context: DrawContext, portId1: Int = 1, portId2: Int = 2): LinearColorGradient =
			getColorGradient(context, portId1, portId2)!!

		fun optionalGradient(context: DrawContext): LinearColorGradient? = getColorGradient(context)
	}
}
