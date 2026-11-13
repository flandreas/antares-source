package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.geom.AffineTransformImpl
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.view.SimpleViewPainter
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.style.EditStyleType
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.*

/** Unit tests for [DrawingViewContentImpl].*/
class DrawingViewContentImplTest {

	init {
		EditTestRule.configure()
	}

	private val drawing = DrawingImpl<Component>()

	private val view = DrawingViewImpl<Component, Drawing<Component>>(
		drawing = drawing,
		transformFactory = { AffineTransformImpl() },
		viewPainterFactory = { SimpleViewPainter(it) }
	)

	private val rect = RectangleComponent(shape = Rectangle2D(0, 0, 10, 10))

	init {
		drawing.add(rect)
	}

	@Test
	fun shouldGetReplacingSelectionModelOfSelectedComponent() {
		view.selectionManager.select(rect)

		assertSame(rect, view.content.getReplacingSelectionModel(rect)?.component)
	}

	@Test
	fun shouldNotGetReplacingSelectionModelAfterDeselecting() {
		view.selectionManager.select(rect)

		view.selectionManager.deselect(rect)

		assertNull(view.content.getReplacingSelectionModel(rect))
	}

	@Test
	fun shouldNotGetReplacingSelectionModelAfterRemovingAllSelectionModels() {
		view.selectionManager.select(rect)

		view.content.removeAllSelectionModels()

		assertNull(view.content.getReplacingSelectionModel(rect))
	}

	@Test
	fun shouldNotGetReplacingSelectionModelForOtherStrategy() {
		rect.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW

		view.selectionManager.select(rect)

		assertNull(view.content.getReplacingSelectionModel(rect))
	}

	@Test
	fun shouldDrawOnlyBackdropComponentsWithinClip() {
		val backdropType: StyleType = EditStyleType("test-backdrop", "test-backdrop", isBackdrop = true)
		val inside = backdropComponent(backdropType, Rectangle2D(0, 0, 10, 10))
		val outside = backdropComponent(backdropType, Rectangle2D(100, 100, 10, 10))
		val context = DrawContext(mock(MockMode.autofill), modelClip = Rectangle2D(5, 5, 10, 10))

		view.content.backdropDrawer.draw(context)

		verify(exactly(1)) { inside.draw(context) }
		verify(exactly(0)) { outside.draw(context) }
	}

	private fun backdropComponent(styleType: StyleType, bbox: Rectangle2D): Component {
		val component = ComponentMockBuilder().withBoundingBox(bbox).build()
		every { component.styleType } returns styleType
		drawing.add(component)
		return component
	}
}
