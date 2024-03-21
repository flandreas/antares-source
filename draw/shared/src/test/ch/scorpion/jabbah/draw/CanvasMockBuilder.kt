package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Dimension2D
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class CanvasMockBuilder {

	private val canvas: Canvas = mockk(relaxed = true)
	private lateinit var dimension: Dimension2D
	private val propertyChangeSlot = slot<PropertyChangeListener<Any>>()
	private val propertyChangeListeners = mutableListOf<PropertyChangeListener<Any>>()

	init {
		every { canvas.addPropertyChangeListener(capture(propertyChangeSlot)) } answers {
			propertyChangeListeners.add(propertyChangeSlot.captured)
		}
		withDevicePixelRatio(1)
	}

	fun withDevicePixelRatio(devicePixelRatio: Int): CanvasMockBuilder {
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