package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.geom.AffineTransformJvm
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule

/**
 * Unit tests for [SelectionManagerImpl].
 */
class SelectionManagerImplTest {

    companion object {
        @ClassRule
        @Suppress("JoinDeclarationAndAssignment")
        lateinit var editTestRule: TestRule

        init {
            editTestRule = EditTestRule()
        }
    }

    val drawing = DrawingImpl<Component>()
    val canvas = CanvasJvm({ DrawingViewImpl<Drawing<Component>>(
        drawing,
        it,
        { AffineTransformJvm() },
        EditSelectModule.selectionManagerFactory,
        EditHighlightModule.highlighterFactory,
        BaseModule.eventBus,
        AnimationModule.animator)}, StyleRepository.INSTANCE)

    var selectionManager: SelectionManagerImpl
    val rect = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))

    init {
        rect.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
        val selectionModelProvider = SimpleSelectionModelProvider(EditSelectModule.selectionModelFactory)
        selectionManager = SelectionManagerImpl(
            canvas.view as DrawingView<out Drawing<Component>>, selectionModelProvider, EventBusImpl())
        drawing.add(rect)
    }

    @Test
    fun shouldSelect() {
        selectionManager.select(rect)
        assertThat(selectionManager.selectionCount, `is`(1))
        assertThat(selectionManager.isSelected(rect), `is`(true))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect), `is`(true))
    }

    @Test
    fun shouldSelectMultiple() {
        val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
        rect2.preferredSelectionDrawingStrategy  = SelectionDrawingStrategy.BELOW
        drawing.add(rect2)
        selectionManager.select(listOf(rect, rect2))
        assertThat(selectionManager.selectionCount, `is`(2))
        assertThat(selectionManager.isSelected(rect), `is`(true))
        assertThat(selectionManager.isSelected(rect2), `is`(true))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect), `is`(true))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect2), `is`(true))
    }

    @Test
    fun shouldSelectAll() {
        val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
        rect2.preferredSelectionDrawingStrategy  = SelectionDrawingStrategy.BELOW
        drawing.add(rect2)
        selectionManager.selectAll()
        assertThat(selectionManager.selectionCount, `is`(2))
        assertThat(selectionManager.isSelected(rect), `is`(true))
        assertThat(selectionManager.isSelected(rect2), `is`(true))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect), `is`(true))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect2), `is`(true))
    }

    @Test
    fun shouldDeselect() {
        selectionManager.select(rect)
        selectionManager.deselect(rect)
        assertThat(selectionManager.selectionCount, `is`(0))
        assertThat(selectionManager.isSelected(rect), `is`(false))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect), `is`(false))
    }

    @Test
    fun shouldDeselectMultiple() {
        val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
        rect2.preferredSelectionDrawingStrategy  = SelectionDrawingStrategy.BELOW
        drawing.add(rect2)
        selectionManager.selectAll()
        selectionManager.deselect(listOf(rect, rect2))
        assertThat(selectionManager.selectionCount, `is`(0))
        assertThat(selectionManager.isSelected(rect), `is`(false))
        assertThat(selectionManager.isSelected(rect2), `is`(false))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect), `is`(false))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect2), `is`(false))
    }

    @Test
    fun shouldDeselectAll() {
        val rect2 = RectangleComponent(styleProvider = StyleRepository.INSTANCE, shape = Rectangle2D(200, 200, 200, 100))
        rect2.preferredSelectionDrawingStrategy  = SelectionDrawingStrategy.BELOW
        drawing.add(rect2)
        selectionManager.selectAll()
        selectionManager.deselectAll()
        assertThat(selectionManager.selectionCount, `is`(0))
        assertThat(selectionManager.isSelected(rect), `is`(false))
        assertThat(selectionManager.isSelected(rect2), `is`(false))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect), `is`(false))
        assertThat(selectionManager.view.content.hasSelectionModelFor(rect2), `is`(false))
    }
}