package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.geom.AffineTransformJvm
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import kotlin.test.*

/**
 * Unit tests for [SelectionManagerImpl].
 */
class SelectionManagerImplTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	val drawing = DrawingImpl<Component>()
	val canvas = CanvasJvm({
		DrawingViewImpl<Drawing<Component>>(
			drawing,
			it,
			{ AffineTransformJvm() },
			EditSelectModule.selectionManagerFactory,
			EditHighlightModule.highlighterFactory,
			BaseModule.eventBus,
			AnimationModule.animator)
	}, StyleRepository.INSTANCE)

	var selectionManager: SelectionManagerImpl
	val rect = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))

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
		val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
		rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		drawing.add(rect2)
		selectionManager.select(listOf(rect, rect2))
		assertEquals(2, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(rect2))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect2))
	}

	@Test
	fun shouldSelectAll() {
		val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
		rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		drawing.add(rect2)
		selectionManager.selectAll()
		assertEquals(2, selectionManager.selectionCount)
		assertTrue(selectionManager.isSelected(rect))
		assertTrue(selectionManager.isSelected(rect2))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect))
		assertTrue(selectionManager.content.hasSelectionModelFor(rect2))
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
		val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
		rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		drawing.add(rect2)
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
		val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
		rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		drawing.add(rect2)
		selectionManager.selectAll()
		selectionManager.deselectAll()
		assertEquals(0, selectionManager.selectionCount)
		assertFalse(selectionManager.isSelected(rect))
		assertFalse(selectionManager.isSelected(rect2))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect))
		assertFalse(selectionManager.content.hasSelectionModelFor(rect2))
	}
}