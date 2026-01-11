package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.geom.Rectangle2D
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DockingControllerTest {

    private val dockingViewBuilder = DockingViewMockBuilder(1000, 1000)

    private val controller = DockingController()

    @BeforeTest
    fun setup() {
        controller.view = dockingViewBuilder.build()
    }

    @Test
    fun shouldSuggestNewRightColumn() {
        // Drag bottom view to right side
        dockingViewBuilder.withColumnRows(2)
        controller.startDragging(CurrentDockingLocation(0, 1))

        val newLoc = controller.mouseDragged(900, 200)!!

        assertEquals(1, newLoc.column.index)
        assertTrue(newLoc.column.insert)
        assertEquals(0, newLoc.row.index)
        assertTrue(newLoc.row.insert)
        assertEquals(Rectangle2D(500, 0, 500, 1000), newLoc.area)
    }

    @Test
    fun shouldSuggestNewLeftColumn() {
        // Drag bottom view to left side
        dockingViewBuilder.withColumnRows(2)
        controller.startDragging(CurrentDockingLocation(0, 1))

        val newLoc = controller.mouseDragged(100, 200)!!

        assertEquals(0, newLoc.column.index)
        assertTrue(newLoc.column.insert)
        assertEquals(0, newLoc.row.index)
        assertTrue(newLoc.row.insert)
        assertEquals(Rectangle2D(0, 0, 500, 1000), newLoc.area)
    }

    @Test
    fun shouldSuggestNewMiddleColumnInFirstColumn() {
        // Drag bottom-right view between the two columns
        dockingViewBuilder.withColumnRows(1, 2)
        controller.startDragging(CurrentDockingLocation(1, 1))

        val newLoc = controller.mouseDragged(480, 900)!!

        assertEquals(1, newLoc.column.index)
        assertTrue(newLoc.column.insert)
        assertEquals(0, newLoc.row.index)
        assertTrue(newLoc.row.insert)
        assertEquals(Rectangle2D(250, 0, 250, 1000), newLoc.area)
    }

    @Test
    fun shouldSuggestNewMiddleColumnInSecondColumn() {
        // Drag bottom-right view between the two columns
        dockingViewBuilder.withColumnRows(1, 2)
        controller.startDragging(CurrentDockingLocation(1, 1))

        val newLoc = controller.mouseDragged(520, 200)!!

        assertEquals(1, newLoc.column.index)
        assertTrue(newLoc.column.insert)
        assertEquals(0, newLoc.row.index)
        assertTrue(newLoc.row.insert)
        assertEquals(Rectangle2D(500, 0, 250, 1000), newLoc.area)
    }

    @Test
    fun shouldSuggestNewTopRow() {
        // Drag bottom-right view to the top of the same column
        dockingViewBuilder.withColumnRows(1, 2)
        controller.startDragging(CurrentDockingLocation(1, 1))

        val newLoc = controller.mouseDragged(600, 100)!!

        assertEquals(1, newLoc.column.index)
        assertFalse(newLoc.column.insert)
        assertEquals(0, newLoc.row.index)
        assertTrue(newLoc.row.insert)
        assertEquals(Rectangle2D(500, 0, 500, 250), newLoc.area)
    }

    @Test
    fun shouldDragLeftColumnToBottomRight() {
        dockingViewBuilder.withColumnRows(1, 1)
        controller.startDragging(CurrentDockingLocation(1, 0))

        val newLoc = controller.mouseDragged(250, 550)

        assertNotNull(newLoc)
        assertEquals(0, newLoc.column.index)
        assertFalse(newLoc.column.insert)
        assertEquals(1, newLoc.row.index)
        assertTrue(newLoc.row.insert)
        assertEquals(Rectangle2D(0, 500, 500, 500), newLoc.area)
    }

    @Test
    fun shouldCalculateBounds() {
        dockingViewBuilder.withColumnRows(1, 3)

        val bounds = controller.getBounds(CurrentDockingLocation(1, 2))
        assertEquals(500, bounds.xInt)
        assertEquals(666, bounds.yInt)
        assertEquals(500, bounds.widthInt)
        assertEquals(333, bounds.heightInt)
    }

    @Test
    fun shouldNotDivideOriginSpace() {
        dockingViewBuilder.withColumnRows(1)
        controller.startDragging(CurrentDockingLocation(0, 0))

        val newLoc = controller.mouseDragged(200, 200)

        assertNotNull(newLoc)
        assertEquals(0, newLoc.column.index)
        assertFalse(newLoc.column.insert)
        assertEquals(0, newLoc.row.index)
        assertFalse(newLoc.row.insert)
        assertEquals(Rectangle2D(0, 0, 1000, 1000), newLoc.area)
    }
}