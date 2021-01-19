package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [SelectionManagerImpl].
 */
class SelectionManagerImplTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val drawing = DrawingImpl<Component>()
	private val canvas = CanvasJvm(EditModule.drawingViewFactory.invoke(drawing))
	private var selectionManager: SelectionManagerImpl
	private val rect = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))

	init {
		rect.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		val selectionModelProvider = SimpleSelectionModelProvider(EditSelectModule.selectionModelFactory)
		selectionManager = SelectionManagerImpl(
			(canvas.view as DrawingView<out Drawing<Component>>).content, selectionModelProvider, EventBusImpl())
		drawing.add(rect)
	}

	@Test
	fun shouldSelect() {
		selectionManager.select(rect)

		assertEquals(1, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
	}

	@Test
	fun shouldSelectMultiple() {
		val rect2 = addRectangle2()

		selectionManager.select(listOf(rect, rect2))

		assertEquals(2, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(rect2))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect2))
	}

	@Test
	fun shouldSelectAll() {
		val rect2 = addRectangle2()

		selectionManager.selectAll()

		assertEquals(2, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(rect2))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect2))
	}

	@Test
	fun shouldNotSelectInvisibleComponent() {
		rect.visible = false

		selectionManager.selectAll()

		assertFalse(selectionManager.isSelected(rect))
	}

	@Test
	fun shouldDeselect() {
		selectionManager.select(rect)

		selectionManager.deselect(rect)

		assertEquals(0, selectionManager.selectionCount)
		assertFalse(selectionManager.isSelected(rect))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect))
	}

	@Test
	fun shouldDeselectMultiple() {
		val rect2 = addRectangle2()

		selectionManager.selectAll()

		selectionManager.deselect(listOf(rect))

		assertEquals(1, selectionManager.selectionCount)
		assertFalse(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(rect2))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect2))
	}

	@Test
	fun shouldDeselectAll() {
		val rect2 = addRectangle2()
		selectionManager.selectAll()

		selectionManager.deselectAll()

		assertEquals(0, selectionManager.selectionCount)
		assertFalse(selectionManager.isSelected(rect))
		assertFalse(selectionManager.isSelected(rect2))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect2))
	}

	@Test
	fun shouldSelectNext() {
		val rect2 = addRectangle2()
		selectionManager.select(rect)

		selectionManager.selectNext()

		assertFalse(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(rect2))
	}

	@Test
	fun shouldSelectFirstAsNext() {
		selectionManager.selectNext()

		assertTrue(selectionManager.isSelected(rect))
	}

	@Test
	fun shouldSelectPrevious() {
		val rect2 = addRectangle2()
		selectionManager.select(rect2)

		selectionManager.selectPrevious()

		assertFalse(selectionManager.isSelected(rect2))
		assertTrue(selectionManager.isSelected(rect))
	}

	@Test
	fun shouldSelectLastAsNext() {
		val rect2 = addRectangle2()

		selectionManager.selectPrevious()

		assertTrue(selectionManager.isSelected(rect2))
	}

	private fun addRectangle2(): RectangleComponent {
		val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
		rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		drawing.add(rect2)
		return rect2
	}
}