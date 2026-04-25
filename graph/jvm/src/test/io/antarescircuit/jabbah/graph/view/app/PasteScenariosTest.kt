package io.antarescircuit.jabbah.graph.view.app

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.app.PasteCommand
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewEditingTest
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests scenarios of "Paste" actions involving displacement calculation depending
 * on whether the paste location is inside the visible view area.
 */
class PasteScenariosTest : AbstractGraphViewEditingTest() {

    private val copyPasteService = GraphViewCopyPasteService()
    private lateinit var vv: TestVerticeView

    override fun setupCircuit() {
        vv = builder.addVerticeView(TestVerticeView.Companion.createEastOutputVerticeView("v1", 100, 100))
        canvasBuilder.withDimension(Dimension2D(500, 500))
        view.navigator.reset()
    }

    /**
     * The first pasted component gets displaced by a standard offset from the origin component
     * (if contained in the visible area).
     */
    @Test
    fun shouldPasteWithStandardDisplacement() {
        val content = copyPasteService.copy(listOf(vv.id), editor.drawing)

        copyPasteService.paste(content, editor.view)

        assertEquals(Point2D(130, 130), builder.build().drawables.first().location)
    }

    /**
     * The origin component and the first pasted component define a distance vector
     * that gets applied to each following paste.
     */
    @Test
    fun shouldPasteArray() {
        val content = copyPasteService.copy(listOf(vv.id), editor.drawing)
        copyPasteService.paste(content, editor.view)
        builder.build().drawables.first().moveBy(70.0, -30.0)
        assertEquals(Point2D(200, 100), builder.build().drawables.first().location)

        copyPasteService.paste(content, editor.view)
        assertEquals(Point2D(300, 100), builder.build().drawables.first().location)

        copyPasteService.paste(content, editor.view)
        assertEquals(Point2D(400, 100), builder.build().drawables.first().location)
    }

    /**
     * If standard placement is outside the visible area, the pasted component must be placed
     * at the mouse location, provided that the mouse is inside the view.
     */
    @Test
    fun shouldPasteAtMouseLocationOutsideVisibleArea() {
        val content = copyPasteService.copy(listOf(vv.id), editor.drawing)
        view.navigator.panBy(-1000, -1000)
        canvasBuilder.withMouseLocation(Point2D(300, 300))

        copyPasteService.paste(content, editor.view)

        assertEquals(Point2D(1290, 1300), builder.build().drawables.first().location)
    }

    /**
     * If the first paste was done at the mouse location because the standard placement was outside
     * the visible area, and the view was paned before undo, redo must place the component at the
     * same location as the first paste.
     */
    @Test
    fun shouldRedoPasteAtSameLocation() {
        val content = copyPasteService.copy(listOf(vv.id), editor.drawing)
        view.navigator.panBy(-1000, -1000)
        canvasBuilder.withMouseLocation(Point2D(300, 300))
        editor.commandManager.reset()

        val pasteInfo = copyPasteService.paste(content, editor.view)
        editor.commandManager.register(PasteCommand(view as DrawingView<Component, Drawing<Component>>, content, pasteInfo, copyPasteService))
        editor.commandManager.undo()

        view.navigator.panBy(1000, 1000)
        editor.commandManager.redo()

        assertEquals(Point2D(1290, 1300), builder.build().drawables.first().location)
    }

    /**
     * If standard placement is outside the visible area, the pasted component must be placed
     * at the center of the view if the mouse is not inside the view.
     */
    @Test
    fun shouldPasteAtViewCenter() {
        val content = copyPasteService.copy(listOf(vv.id), editor.drawing)
        view.navigator.panBy(-1000, -1000)
        canvasBuilder.withMouseLocation(Point2D(10_000, 10_000))

        copyPasteService.paste(content, editor.view)

        assertEquals(Point2D(1240, 1250), builder.build().drawables.first().location)
    }

    @Test
    fun shouldNotFallBackToArrayMode() {
        val content = copyPasteService.copy(listOf(vv.id), editor.drawing)
        copyPasteService.paste(content, editor.view)

        // Setup array mode with dx = 100, dy = 0.
        builder.build().drawables.first().moveBy(70.0, -30.0)
        assertEquals(Point2D(200, 100), builder.build().drawables.first().location)

        // Paste once in array mode
        copyPasteService.paste(content, editor.view)
        assertEquals(Point2D(300, 100), builder.build().drawables.first().location)

        // Change the visible area to place the next paste at the mouse location
        view.navigator.panBy(-500, 0)
        canvasBuilder.withMouseLocation(Point2D(200, 200))
        copyPasteService.paste(content, editor.view)
        assertEquals(Point2D(690, 200), builder.build().drawables.first().location)

        // In array mode, this would now be at (500, 100) and again in the visible area,
        // but since array mode was interrupted with "Place at mouse location", the system
        // should continue with "Place at mouse location". Or with "Place at view center"
        // if mouse is outside view.
        copyPasteService.paste(content, editor.view)
        assertEquals(Point2D(690, 200), builder.build().drawables.first().location)
    }
}