package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.style.StyleRepository
import io.antarescircuit.jabbah.draw.view.CanvasJvm
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.module.EditModule
import kotlin.test.*

/**
 * Unit tests for [SelectionManagerImpl].
 */
class SelectionManagerImplTest {

	private val eventBus = EventBusImpl()
	private val drawing: DrawingImpl<Component>
	private val canvas: CanvasJvm
	private var selectionManager: SelectionManagerImpl
	private val rect: RectangleComponent

	init {
		EditTestRule.configure()
		drawing = DrawingImpl()
		canvas = CanvasJvm(EditModule.drawingViewFactory.create(drawing, null, false, ""))
		rect = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))

		rect.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		val selectionModelProvider = SimpleSelectionModelProvider(EditSelectModule.selectionModelFactory)
		selectionManager = SelectionManagerImpl(
			(canvas.view as DrawingView<*, *>).content, selectionModelProvider, eventBus)
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
		val rect = addRectangle()

		selectionManager.select(listOf(this.rect, rect))

		assertEquals(2, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(this.rect))
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(this.rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
	}

	@Test
	fun shouldSelectAll() {
		val rect = addRectangle()

		selectionManager.selectAll()

		assertEquals(2, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(this.rect))
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(this.rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
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
		val rect = addRectangle()

		selectionManager.selectAll()

		selectionManager.deselect(listOf(this.rect))

		assertEquals(1, selectionManager.selectionCount)
		assertFalse(selectionManager.isSelected(this.rect))
		assertTrue(selectionManager.isSelected(rect))
		assertFalse(selectionManager.content.hasSelectionModelFor(this.rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
	}

	@Test
	fun shouldDeselectAll() {
		val rect = addRectangle()
		selectionManager.selectAll()

		selectionManager.deselectAll()

		assertEquals(0, selectionManager.selectionCount)
		assertFalse(selectionManager.isSelected(this.rect))
		assertFalse(selectionManager.isSelected(rect))
		assertFalse(selectionManager.content.hasSelectionModelFor(this.rect))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect))
	}

	@Test
	fun shouldSelectNext() {
		val rect = addRectangle()
		selectionManager.select(this.rect)

		selectionManager.selectNext()

		assertFalse(selectionManager.isSelected(this.rect))
		assertTrue(selectionManager.isSelected(rect))
	}

	@Test
	fun shouldSelectFirstAsNext() {
		selectionManager.selectNext()

		assertTrue(selectionManager.isSelected(rect))
	}

	@Test
	fun shouldSelectPrevious() {
		val rect = addRectangle()
		selectionManager.select(rect)

		selectionManager.selectPrevious()

		assertFalse(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(this.rect))
	}

	@Test
	fun shouldSelectLastAsNext() {
		val rect = addRectangle()

		selectionManager.selectPrevious()

		assertTrue(selectionManager.isSelected(rect))
	}

	@Test
	fun shouldReplaceSelectionIfDifferent() {
		val events = mutableListOf<SelectionChangeEvent>()
		val rect1 = addRectangle()
		val rect2 = addRectangle()
		selectionManager.select(rect1)
		eventBus.register(SelectionChangeEvent::class) { events.add(it) }

		selectionManager.replace { it === rect2 }

		assertFalse(selectionManager.isSelected(rect1))
		assertTrue(selectionManager.isSelected(rect2))
		assertEquals(SelectionChangeEvent.Type.DESELECTED, events[0].type)
		assertSame(rect1, events[0].components.iterator().next())
		assertEquals(SelectionChangeEvent.Type.SELECTED, events[1].type)
		assertSame(rect2, events[1].components.iterator().next())
	}

	@Test
	fun shouldNotReplaceSelectionIfSame() {
		val events = mutableListOf<SelectionChangeEvent>()
		val rect1 = addRectangle()
		val rect2 = addRectangle()
		selectionManager.select(listOf(rect1, rect2))
		eventBus.register(SelectionChangeEvent::class) { events.add(it) }

		selectionManager.replace { it === rect1 || it === rect2 }

		assertTrue(selectionManager.isSelected(rect1))
		assertTrue(selectionManager.isSelected(rect2))
		assertTrue(events.isEmpty())
	}

	private fun addRectangle(): RectangleComponent {
		return RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100)).also {
			it.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
			drawing.add(it)
		}
	}
}