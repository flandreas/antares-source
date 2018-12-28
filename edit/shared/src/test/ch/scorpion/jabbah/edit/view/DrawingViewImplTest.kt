package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.view.SimpleViewPainter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.select.EditSelectModule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

/** Unit tests for [DrawingViewImpl].*/
class DrawingViewImplTest {

	companion object {
		@ClassRule
		@Suppress("JoinDeclarationAndAssignment")
		lateinit var editTestRule: TestRule

		init {
			editTestRule = EditTestRule()
		}
	}

	private val drawing = DrawingImpl<Component>()

	private val canvas: Canvas = createCanvas()

	private val view = DrawingViewImpl<Drawing<Component>>(
		drawing = drawing,
		canvas = canvas,
		transformFactory = { AffineTransformImpl() },
		viewPainterFactory = { SimpleViewPainter(it) })

	init {
		Mockito.clearInvocations(canvas)
	}

	@Test
	fun shouldRepaintCanvasWhenValidatingDrawing() {
		drawing.validate()

		verify(canvas).repaint()
	}

	@Test
	fun shouldNotRepaintValidatedReplacedContent() {
		val oldContent = view.content
		val newDrawing = DrawingImpl<Component>()
		val newContent = DrawingViewContentImpl(view, newDrawing, EditSelectModule.selectionManagerFactory.create(view), EditHighlightModule.highlighterFactory)
		view.content = newContent

		Mockito.clearInvocations(canvas)
		oldContent.drawing.validate()

		verify(canvas, never()).repaint()
	}

	@Test
	fun shouldRepaintValidatedReplacingContent() {
		val newDrawing = DrawingImpl<Component>()
		val newContent = DrawingViewContentImpl(view, newDrawing, EditSelectModule.selectionManagerFactory.create(view), EditHighlightModule.highlighterFactory)
		view.content = newContent

		Mockito.clearInvocations(canvas)
		newContent.drawing.validate()

		verify(canvas).repaint()
	}

	@Test
	fun shouldRepaintValidatedReusedContent() {
		val oldContent = view.content
		val newDrawing = DrawingImpl<Component>()
		val newContent = DrawingViewContentImpl(view, newDrawing, EditSelectModule.selectionManagerFactory.create(view), EditHighlightModule.highlighterFactory)
		view.content = newContent
		view.content = oldContent

		Mockito.clearInvocations(canvas)
		oldContent.drawing.validate()

		verify(canvas).repaint()
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mock()
		whenever(canvas.view).thenReturn(view)
		whenever(canvas.dimension).thenReturn(Dimension2D(100, 100))
		return canvas
	}

}