package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.JTreeUtil
import io.antarescircuit.jabbah.base.swing.JTreeUtil.findTreeNode
import io.antarescircuit.jabbah.base.swing.JTreeUtil.getPath
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics.PROP_TREE_SHOW_ROOT_HANDLES
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.richtext.RichTextLabel
import io.antarescircuit.jabbah.edit.model.text.NamableTreeNode
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.ui.MetaGraphIconProvider
import io.antarescircuit.jabbah.graph.ui.library.BasicLibraryTreeView
import io.antarescircuit.jabbah.graph.ui.library.BasicLibraryTreeViewController
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel

open class BasicLibraryTreeViewSwing<T: BasicLibraryTreeView>(
    protected var basicController: BasicLibraryTreeViewController<T>,
    showWorkspaceNode: Boolean = true,
    includeImports: Boolean = true
) : JTree(
    LibraryTreeModelBuilderSwing(basicController.library, includeImports = includeImports).build()),
    BasicLibraryTreeView
{
    private val showBeginnerTips = BaseModule.properties.getBoolean(PROP_BEGINNER_HELP_TOOLTIP)

    companion object {
        private val LOG by logger(BasicLibraryTreeViewSwing::class)
    }

    init {
        basicController.view = this as T
        minimumSize = Dimension(super.getMinimumSize().width, 200)

        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        isRootVisible = showWorkspaceNode
        setShowsRootHandles(BaseModule.properties.getBoolean(PROP_TREE_SHOW_ROOT_HANDLES))

        setCellRenderer(Renderer())
        addTreeSelectionListener { basicController.selectedItem = getSelectedItem() }

        ToolTipManager.sharedInstance().registerComponent(this)

        expandRow(0)
    }

    override fun dispose() {
        ToolTipManager.sharedInstance().unregisterComponent(this)
    }

    /** ---- [BasicLibraryTreeView] interface */

    override fun refresh() {
        invalidate()
        repaint()
    }

    override fun reload() {
        model = LibraryTreeModelBuilderSwing(basicController.library).build()
    }

    override fun expandTo(element: ContainerLibraryElement) {
        SwingUtilities.invokeLater {
            val node = findTreeNode(treeModel.root as TreeNode) { (it as DefaultMutableTreeNode).userObject === element }
            if (node != null) {
                getPath(node).also {
                    selectionPath = it
                    scrollPathToVisible(it)
                }
            }
        }
    }

    override fun expandFolder(folderName: String) {
        SwingUtilities.invokeLater {
            val node = findTreeNode(treeModel.root as TreeNode) {
                (it as DefaultMutableTreeNode).userObject is LibraryFolder
                        && (it.userObject as LibraryFolder).name.getTranslation(Language.English) == folderName
            }
            if (node != null) {
                getPath(node).also {
                    expandPath(it)
                }
            }
        }
    }

    override fun expandToCurrentSavable() {
        expandRow(0)
        if (basicController.currentSavable is AbstractLibraryItemSavable) {
            findOptionalTreeNode((basicController.currentSavable as AbstractLibraryItemSavable).item)?.let {
                if (it.userObject is ContainerLibraryElement) {
                    expandTo(it.userObject as ContainerLibraryElement)
                }
            }
        }
    }

    override fun expandAllFromSelection() {
        selectionPath?.let {
            JTreeUtil.expandAll(this, it)
        }
    }

    override fun collapseAtSelection() {
        selectionPath?.let {
            JTreeUtil.collapseAll(this, it)
        }
    }

    override fun openMainLibrary(library: Library) {
        model = LibraryTreeModelBuilderSwing(library).withFont(Graphics2DJvm.fromAwtFont(font)).build()
        expandRow(0)

        if (library.expandedImports.staleImportCount > 0) {
            var title: String
            var text: String
            if (library is Project) {
                title = Translations.getString("library.open.staleReferenceFromProject.name")
                text = Translations.getString("library.open.staleReferenceFromProject.msg")
            } else {
                title = Translations.getString("library.open.staleReferenceFromLibrary.name")
                text = Translations.getString("library.open.staleReferenceFromLibrary.msg")
            }
            SwingUtilities.invokeLater {
                JOptionPane.showConfirmDialog(
                    Frame.getFrames()[0],
                    text,
                    title,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE
                )
            }
        }
    }

    override fun closeMainLibrary() {
        val root = model.root as DefaultMutableTreeNode
        root.removeAllChildren()
        (model as DefaultTreeModel).nodeStructureChanged(root)
    }

    /** ---- [BasicLibraryTreeViewSwing] */

    private fun getSelectedItem(): LibraryItem? {
        val path = selectionPath ?: return null
        if ((path.lastPathComponent as DefaultMutableTreeNode).userObject is LibraryItem?) {
            return (path.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryItem?
        }
        return null
    }

    /** Finds the [TreeNode] that contains the specified [LibraryItem] as user object.*/
    protected fun findTreeNode(item: LibraryItem): DefaultMutableTreeNode {
        return findOptionalTreeNode(item)!!
    }

    protected fun findOptionalTreeNode(item: LibraryItem): DefaultMutableTreeNode? {
        return findTreeNode(model.root as TreeNode) {
            (it as DefaultMutableTreeNode).userObject == item
        } as DefaultMutableTreeNode?
    }

    private inner class Renderer : RichTextLabel() {

        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
        private val projectIcon = UiUtil.themedIcon("/img/project.png")
        private val libraryIcon = UiUtil.themedIcon("/img/library.png")
        private val libraryImportIcon = UiUtil.themedIcon("/img/imported-library.png")
        private val brokenImportIcon = UiUtil.themedIcon("/img/broken-import.png")
        private val folderIcon = UiUtil.themedIcon("/img/folder.png")
        private val desktopIcon = UiUtil.themedIcon("/img/table.png")

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel
            component.richText = null
            component.toolTipText = null
            component.verticalTextPosition = SwingConstants.CENTER
            component.border = null
            if ((value as DefaultMutableTreeNode).userObject is LibraryItem) {
                val iconPath = (value.userObject as LibraryItem).iconPath
                component.font = this@BasicLibraryTreeViewSwing.font
                if (StringUtils.isNotEmpty(iconPath)) {
                    if (basicController.isCurrentItem(value.userObject as LibraryItem) &&(value.userObject as LibraryItem).activeIconPath != null) {
                        component.icon = getIcon((value.userObject as LibraryItem).activeIconPath!!)
                    } else {
                        component.icon = getIcon(iconPath!!)
                    }
                    component.richText = (value as NamableTreeNode).richTextName.value
                }
                if (value.userObject is ContainerLibraryElement) {
                    val cle = value.userObject as ContainerLibraryElement
                    component.richText = (value as NamableTreeNode).richTextName.value
                    if (showBeginnerTips) {
                        component.toolTipText = Translations.getString("library.action.libraryElement.tip")
                    }
                    component.icon = MetaGraphIconProvider.provideIcon(cle.graphType, basicController.isCurrentItem(cle), false)
                    component.richText?.underline = basicController.isDefaultElement(cle)
                } else if (value.userObject is Project) {
                    component.icon = projectIcon
                } else if (value.userObject is Library) {
                    if (value.userObject === basicController.library) {
                        component.icon = libraryIcon
                    } else {
                        if ((value.userObject as Library).isBrokenImport) {
                            component.icon = brokenImportIcon
                        } else {
                            component.icon = libraryImportIcon
                        }
                    }
                } else if (value.userObject is LibraryFolder) {
                    component.icon = folderIcon
                } else if (value.userObject is BaseLibraryElement || value.userObject is ImageLibraryElement) {
                    if (showBeginnerTips) {
                        component.toolTipText = Translations.getString("library.action.baseElement.tip")
                    }
                } else {
                    if (showBeginnerTips) {
                        component.toolTipText = Translations.getString("library.action.openElement.tip")
                    }
                }
            } else {
                component.icon = desktopIcon
            }
            return component
        }

        private fun getIcon(iconPath: String): Icon {
            return iconCache.getOrPut(iconPath) { UiUtil.themedIcon(iconPath) }
        }
    }
}