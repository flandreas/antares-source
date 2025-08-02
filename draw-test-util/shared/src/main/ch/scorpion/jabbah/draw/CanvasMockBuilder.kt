package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Dimension2D
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock

class CanvasMockBuilder {

	private val canvas: Canvas = mock(MockMode.autofill)
	private lateinit var dimension: Dimension2D
	private val propertyChangeSlot = Capture.slot<PropertyChangeListener<Any>>()
	private val propertyChangeListeners = mutableListOf<PropertyChangeListener<Any>>()

	init {
		every { canvas.addPropertyChangeListener(capture(propertyChangeSlot)) } calls  {
			propertyChangeListeners.add(propertyChangeSlot.get())
		}
		withDevicePixelRatio(1.0)
		withDimension(Dimension2D(0, 0))
	}

	fun withDevicePixelRatio(devicePixelRatio: Double): CanvasMockBuilder {
		every { canvas.devicePixelRatio } returns devicePixelRatio
		return this
	}

	fun withView(view: View<*>): CanvasMockBuilder {
		every { canvas.view } returns view
		view.canvas = canvas
		propertyChangeListeners.forEach {
			it.propertyChanged(PropertyChangeEvent(canvas, Canvas.PROP_DIMENSION, null, dimension))
		}
		return this
	}

	fun withDimension(dimension: Dimension2D): CanvasMockBuilder {
		this.dimension = dimension
		every { canvas.dimension } returns dimension
		return this
	}

	fun build(): Canvas = canvas
}