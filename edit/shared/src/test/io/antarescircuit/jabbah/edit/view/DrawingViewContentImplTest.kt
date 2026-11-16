package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.geom.AffineTransformImpl
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
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
		val backdropType: StyleType = backdropStyleType()
		val inside = backdropComponent(backdropType, Rectangle2D(0, 0, 10, 10))
		val outside = backdropComponent(backdropType, Rectangle2D(100, 100, 10, 10))
		val context = DrawContext(mock(MockMode.autofill), modelClip = Rectangle2D(5, 5, 10, 10))

		view.content.backdropDrawer.draw(context)

		verify(exactly(1)) { inside.draw(context) }
		verify(exactly(0)) { outside.draw(context) }
	}

	@Test
	fun shouldReuseBackdropMembershipAfterInvalidationAndGeometryUpdates() {
		rect.styleType = backdropStyleType()
		val backdrops = drawing.getBackdropDrawables()
		assertEquals(listOf(rect), backdrops)

		rect.invalidate()
		rect.validate()
		rect.moveBy(10.0, 0.0)
		rect.update()

		assertSame(backdrops, drawing.getBackdropDrawables())
	}

	@Test
	fun shouldRefreshBackdropMembershipWhenStyleChanges() {
		val backdropType = backdropStyleType()
		assertTrue(drawing.getBackdropDrawables().isEmpty())

		rect.styleType = backdropType
		assertEquals(listOf(rect), drawing.getBackdropDrawables())
		rect.styleType = StyleType.FIGURE
		assertTrue(drawing.getBackdropDrawables().isEmpty())
	}

	@Test
	fun shouldRefreshBackdropMembershipAfterAddingRemovingAndClearing() {
		val backdropType = backdropStyleType()
		assertTrue(drawing.getBackdropDrawables().isEmpty())
		rect.styleType = backdropType
		assertEquals(listOf(rect), drawing.getBackdropDrawables())

		val other = RectangleComponent(shape = Rectangle2D(20, 20, 10, 10))
		other.styleType = backdropType
		drawing.add(other)
		assertEquals(listOf(other, rect), drawing.getBackdropDrawables())
		drawing.remove(other)
		assertEquals(listOf(rect), drawing.getBackdropDrawables())
		drawing.clear()
		assertTrue(drawing.getBackdropDrawables().isEmpty())
	}

	@Test
	fun shouldRefreshBackdropOrderAfterRestacking() {
		val backdropType = backdropStyleType()
		rect.styleType = backdropType
		val other = RectangleComponent(shape = Rectangle2D(20, 20, 10, 10))
		other.styleType = backdropType
		drawing.add(other)
		assertEquals(listOf(other, rect), drawing.getBackdropDrawables())

		drawing.setStackingOrderPosition(0, rect.id)

		assertEquals(listOf(rect, other), drawing.getBackdropDrawables())
	}

	private fun backdropStyleType(): StyleType =
		EditStyleType("test-backdrop", "test-backdrop", isBackdrop = true).also {
			DrawStyleModule.styleProvider.registerStyle(it, DrawStyleModule.styleProvider.getStyle(StyleType.FIGURE))
		}

	private fun backdropComponent(styleType: StyleType, bbox: Rectangle2D): Component {
		val component = ComponentMockBuilder().withBoundingBox(bbox).build()
		every { component.styleType } returns styleType
		drawing.add(component)
		return component
	}
}
