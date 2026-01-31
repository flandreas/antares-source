package ch.scorpion.jabbah.graph.ui.desktop

import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertSame

class DockingGraphDesktopViewItemsTest {

    private var desktopViewItems = DockingGraphDesktopViewItems(2)

    @Test
    fun shouldMoveRightToBottomLeft() {
        val items = showMainWithChildItems(1)

        desktopViewItems.move(items[1], NewDockingLocation.newRow(0, 1))

        assertEquals(1, desktopViewItems.columnsCount)
        assertEquals(2, desktopViewItems.getRowsCount(0))
        assertSame(items[0], desktopViewItems.getItem(0, 0))
        assertSame(items[1], desktopViewItems.getItem(0, 1))
    }

    @Test
    fun shouldMoveRightToTopLeft() {
        val items = showMainWithChildItems(1)

        desktopViewItems.move(items[1], NewDockingLocation.newRow(0, 0))

        assertEquals(1, desktopViewItems.columnsCount)
        assertEquals(2, desktopViewItems.getRowsCount(0))
        assertSame(items[1], desktopViewItems.getItem(0, 0))
        assertSame(items[0], desktopViewItems.getItem(0, 1))
    }

    @Test
    fun shouldMoveRightToLeft() {
        val items = showMainWithChildItems(1)

        desktopViewItems.move(items[1], NewDockingLocation.newColumn(0))

        assertEquals(2, desktopViewItems.columnsCount)
        assertEquals(1, desktopViewItems.getRowsCount(0))
        assertEquals(1, desktopViewItems.getRowsCount(1))
        assertSame(items[1], desktopViewItems.getItem(0, 0))
        assertSame(items[0], desktopViewItems.getItem(1, 0))
    }

    @Test
    fun shouldMoveLeftToRight() {
        val items = showMainWithChildItems(1)

        desktopViewItems.move(items[0], NewDockingLocation.newColumn(2))

        assertEquals(2, desktopViewItems.columnsCount)
        assertEquals(1, desktopViewItems.getRowsCount(0))
        assertEquals(1, desktopViewItems.getRowsCount(1))
        assertSame(items[1], desktopViewItems.getItem(0, 0))
        assertSame(items[0], desktopViewItems.getItem(1, 0))
    }

    @Test
    fun shouldMoveLeftToBottomRight() {
        val items = showMainWithChildItems(1)

        desktopViewItems.move(items[0], NewDockingLocation.newRow(1, 1))

        assertEquals(1, desktopViewItems.columnsCount)
        assertEquals(2, desktopViewItems.getRowsCount(0))
        assertSame(items[1], desktopViewItems.getItem(0, 0))
        assertSame(items[0], desktopViewItems.getItem(0, 1))
    }

    @Test
    fun shouldMoveLeftToMiddle() {
        val items = showMainWithChildItems(3) // results in 3 columns

        desktopViewItems.move(items[0], NewDockingLocation.newColumn(2))

        assertEquals(3, desktopViewItems.columnsCount)
        assertEquals(2, desktopViewItems.getRowsCount(0))
        assertEquals(1, desktopViewItems.getRowsCount(1))
        assertEquals(1, desktopViewItems.getRowsCount(1))
        assertSame(items[1], desktopViewItems.getItem(0, 0))
        assertSame(items[2], desktopViewItems.getItem(0, 1))
        assertSame(items[0], desktopViewItems.getItem(1, 0))
        assertSame(items[3], desktopViewItems.getItem(2, 0))
    }

    @Test
    fun shouldMoveTopToMiddle() {
        desktopViewItems = DockingGraphDesktopViewItems(3)
        val items = showMainWithChildItems(3)

        desktopViewItems.move(items[1], NewDockingLocation.newRow(1, 2))

        assertEquals(2, desktopViewItems.columnsCount)
        assertEquals(1, desktopViewItems.getRowsCount(0))
        assertEquals(3, desktopViewItems.getRowsCount(1))
        assertSame(items[0], desktopViewItems.getItem(0, 0))
        assertSame(items[2], desktopViewItems.getItem(1, 0))
        assertSame(items[1], desktopViewItems.getItem(1, 1))
        assertSame(items[3], desktopViewItems.getItem(1, 2))
    }

    @Test
    fun shouldMoveSecondTopToThird() {
        val items = showMainWithChildItems(2)

        desktopViewItems.move(items[1], NewDockingLocation.newColumn(2))

        assertEquals(3, desktopViewItems.columnsCount)
        assertEquals(1, desktopViewItems.getRowsCount(0))
        assertEquals(1, desktopViewItems.getRowsCount(1))
        assertEquals(1, desktopViewItems.getRowsCount(2))
        assertSame(items[0], desktopViewItems.getItem(0, 0))
        assertSame(items[2], desktopViewItems.getItem(1, 0))
        assertSame(items[1], desktopViewItems.getItem(2, 0))
    }

    private fun showMainWithChildItems(childCount: Int): List<DummyDesktopViewItem> {
        var index = 0
        val items = mutableListOf<DummyDesktopViewItem>()
        desktopViewItems.addMainItem(DummyDesktopViewItem((index++).toString()).also { items.add(it) })
        for (i in 1..childCount) {
            desktopViewItems.addChildItem(DummyDesktopViewItem((index++).toString()).also { items.add(it) })
        }
        return items
    }
}