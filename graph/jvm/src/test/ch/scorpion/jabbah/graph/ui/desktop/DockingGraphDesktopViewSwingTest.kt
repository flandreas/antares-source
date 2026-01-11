package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import dev.mokkery.MockMode.autofill
import dev.mokkery.mock
import javax.swing.JPanel
import javax.swing.JSplitPane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DockingGraphDesktopViewSwingTest {

    private val controller: GraphDesktopViewController = GraphDesktopViewController(GraphApplicationContextHolder(mock(autofill)))
    private val view: DockingGraphDesktopViewSwing = DockingGraphDesktopViewSwing(controller)

    @Test
    fun shouldShowMainItemDirectly() {
        val items = showMainWithChildItems(0)

        assertEquals(1, view.columns.size)
        assertSame(items[0], view.contentComponent.getComponent(0))
    }

    @Test
    fun shouldShowChildInNewColumn() {
        val items = showMainWithChildItems(1)

        assertEquals(2, view.columns.size)
        val splitPane = view.contentComponent as JSplitPane
        assertSame(items[0], (splitPane.leftComponent as JPanel).getComponent(0))
        assertSame(items[1], (splitPane.rightComponent as JPanel).getComponent(0))
    }

    @Test
    fun shouldAddChildToExistingColumn() {
        val items = showMainWithChildItems(2)

        assertEquals(2, view.columns.size)
        val splitPane = view.contentComponent as JSplitPane
        assertSame(items[0], (splitPane.leftComponent as JPanel).getComponent(0))
        assertSame(items[1], (splitPane.rightComponent as JPanel).getComponent(0))
        assertSame(items[2], (splitPane.rightComponent as JPanel).getComponent(1))
    }

    @Test
    fun shouldCreateNewColumnIfColumnIsFull() {
        val items = showMainWithChildItems(3)

        assertEquals(3, view.columns.size)
        val splitPane0 = view.contentComponent as JSplitPane
        val splitPane1 = splitPane0.rightComponent as JSplitPane
        assertEquals(2, (splitPane1.leftComponent as JPanel).componentCount)
        assertEquals(1, (splitPane1.rightComponent as JPanel).componentCount)
        assertSame(items[3], (splitPane1.rightComponent as JPanel).getComponent(0))
    }

    @Test
    fun shouldCloseOneOfManyInColumn() {
        val items = showMainWithChildItems(2)

        view.closeChildItem(items[2])

        assertEquals(2, view.columns.size)
        assertEquals(1, view.columns[0].size)
        assertEquals(1, view.columns[1].size)
        val splitPane = view.contentComponent as JSplitPane
        assertSame(items[0], (splitPane.leftComponent as JPanel).getComponent(0))
        assertSame(items[1], (splitPane.rightComponent as JPanel).getComponent(0))
    }

    @Test
    fun shouldCloseLastInColumn() {
        val items = showMainWithChildItems(3)

        view.closeChildItem(items[3])

        assertEquals(2, view.columns.size)
        assertEquals(1, view.columns[0].size)
        assertEquals(2, view.columns[1].size)
        val splitPane = view.contentComponent as JSplitPane
        assertSame(items[0], (splitPane.leftComponent as JPanel).getComponent(0))
        assertSame(items[1], (splitPane.rightComponent as JPanel).getComponent(0))
        assertSame(items[2], (splitPane.rightComponent as JPanel).getComponent(1))
    }

    @Test
    fun shouldGetCurrentLocationOfMain() {
        val items = showMainWithChildItems(0)
        with(view.getCurrentLocationOf(items[0])) {
            assertEquals(0, column)
            assertEquals(0, row)
        }
    }

    @Test
    fun shouldGetCurrentLocationOfLastInColumn() {
        val items = showMainWithChildItems(2)
        with(view.getCurrentLocationOf(items[2])) {
            assertEquals(1, column)
            assertEquals(1, row)
        }
    }

    private class DummyDesktopViewItem(
        private val item: GraphDesktopViewItem
    ) : JPanel(), GraphDesktopViewItem by item {

        constructor() : this(mock())
    }

    private fun showMainWithChildItems(childCount: Int): List<DummyDesktopViewItem> {
        val items = mutableListOf<DummyDesktopViewItem>()
        view.showMainItem(DummyDesktopViewItem().also { items.add(it) })
        for (i in 1..childCount) {
            view.showChildItem(DummyDesktopViewItem().also { items.add(it) })
        }
        return items
    }
}