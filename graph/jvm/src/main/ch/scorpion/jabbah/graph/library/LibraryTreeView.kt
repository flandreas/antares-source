package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import java.awt.Component
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import java.awt.event.InputEvent
import javax.swing.*
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * Displays the {@link Library} as a tree.
 *
 * Instances of this class post the following events on the {@link T2Bus}:
 * - A [LibrarySelectionChangedEvent] when the user selects a tree item
 * - A [OpenContainerLibraryElementRequest] when the user double clicks on a [ContainerLibraryElement]
 */
class LibraryTreeView(
    private val eventBus: EventBus,
    private val libraryHolder: LibraryHolder
) : JTree(createLibraryTreeModel(libraryHolder.library)) {

    @Suppress("unused")
    constructor(): this(BaseModule.eventBus, LibraryModule.libraryHolder)

    init {
        transferHandler = LibraryElementTransferHandler()

        setRowHeight(24)
        setCellRenderer(Renderer())
        addTreeSelectionListener({eventBus.post(LibrarySelectionChangedEvent(this))})
        addMouseListener(DoubleClickListener())

        dragEnabled = true
        dropMode = DropMode.ON

        // TODO Update TreeModel selectively
        eventBus.register(LibraryItemAddedEvent::class, { updateLibrary() })
        eventBus.register(LibraryItemRemovedEvent::class, { updateLibrary() })
        eventBus.register(LibraryItemUpdatedEvent::class, { updateLibrary() })

        eventBus.register(ApplicationModeEvent::class, { dragEnabled = it.applicationMode === ApplicationMode.EDIT })
    }

    companion object {

        fun createLibraryTreeModel(library: Library): TreeModel {
            val node = DefaultMutableTreeNode(library)
            addItems(node, library)
            return DefaultTreeModel(node)
        }

        private fun addItems(parent: DefaultMutableTreeNode, directory: LibraryDirectory) {
            // TODO Sort
            for (item in directory.getItems()) {
                val node = DefaultMutableTreeNode(item)
                parent.add(node)
                if (item is LibraryDirectory) {
                    addItems(node, item)
                }
            }
        }
    }


    /** ---- [LibraryTreeView] */

    fun getSelectedItem(): LibraryItem? {
        val path = selectionPath ?: return null
        return (path.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryItem
    }

    private fun updateLibrary() {
        SwingUtilities.invokeLater {
            model = createLibraryTreeModel(libraryHolder.library)
        }
    }

    private inner class DoubleClickListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (e.clickCount == 2 && getSelectedItem() is ContainerLibraryElement) {
                eventBus.post(OpenContainerLibraryElementRequest(getSelectedItem() as ContainerLibraryElement, e))
            }
        }
    }

    private class Renderer : DefaultTreeCellRenderer() {

        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
        private val containerLibElemIcon = ContainerLibraryElementIcon()

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
            val iconPath = ((value as DefaultMutableTreeNode).userObject as LibraryItem).iconPath
            if (StringUtils.isNotEmpty(iconPath)) {
                component.icon = getIcon(iconPath!!)
            } else if (value.userObject is ContainerLibraryElement) {
                component.icon = containerLibElemIcon
            }
            return component
        }

        private fun getIcon(iconPath: String): Icon {
            return iconCache.getOrPut(iconPath, { ImageIcon(LibraryTreeView::class.java.getResource(iconPath)) })
        }
    }

    /** Defines an artificial [Icon] to be used as tree icon for [ContainerLibraryElement]s.*/
    private class ContainerLibraryElementIcon : Icon {
        override fun getIconHeight(): Int = 28
        override fun getIconWidth(): Int = 28

        override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
            g?.color = Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.backgroundColor)
            g?.fillRect(5, 5, 14, 18)
            g?.color = Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.foregroundColor)
            g?.drawRect(5, 5, 14, 18)

            g?.drawLine(5, 10, 1, 10)
            g?.drawLine(5, 20, 1, 20)
            g?.drawLine(21, 14, 25, 14)
        }
    }
}

/**
 * Posted on a [LibraryTreeView]'s [EventBus] if the current selection in this [LibraryTreeView] has changed.
 */
data class LibrarySelectionChangedEvent(val libraryTreeView: LibraryTreeView)

/**
 * Represents a request to open the [MetaGraph] of a [ContainerLibraryElement].
 * This request is posted by [LibraryTreeView]. It is up to higher level application classes to decide how the
 * [MetaGraph] of the selected [ContainerLibraryElement] is to be presented to the user.
 *
 * @property element the [ContainerLibraryElement] whose [MetaGraph] is to be opened
 * @property inputEvent the [InputEvent] with which the user tried to open the [ContainerLibraryElement]. Can be used to
 * implement different application level UI strategies based e.g. on the pressed key.
 */
data class OpenContainerLibraryElementRequest(val element: ContainerLibraryElement, val inputEvent: InputEvent)