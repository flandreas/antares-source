package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.mreact.IconProviderRegistry
import ch.scorpion.jabbah.base.mreact.jmTreeItem
import ch.scorpion.jabbah.base.mreact.jmTreeView
import ch.scorpion.jabbah.edit.ui.DragAndDropDepo
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewActions
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import kotlinx.browser.document
import kotlinx.css.*
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import react.*
import styled.css

external interface LibraryTreeViewJsProps : RProps {
	var application: Application
	var controller: LibraryTreeViewController
	var onDoubleClick: (node: LibraryTreeNode) -> Unit
}

fun RBuilder.libraryTreeView(handler: LibraryTreeViewJsProps.() -> Unit): ReactElement {
	return child(LibraryTreeViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryTreeViewJs(
	props: LibraryTreeViewJsProps
) : RComponent<LibraryTreeViewJsProps, RState>(props), LibraryTreeView {

	companion object {
		private val LOG by logger(LibraryTreeViewJs::class)
	}

	private val actions = LibraryTreeViewActions(props.controller, props.application)
	private var nodeId: Int = 0

	// Used for suppressing the default DnD image
	private var emptyDragImage: HTMLCanvasElement? = null

	init {
		props.controller.view = this
	}

	/** ---- [RComponent] */

	override fun RBuilder.render() {
		nodeId = 0

		jmTreeView(
			defaultExpandIcon = createExpandIcon(),
			defaultCollapseIcon = createCollapseIcon(),
		) {
			css {
				paddingLeft = 2.spacingUnits
				maxHeight = LinearDimension.fillAvailable
				maxWidth = LinearDimension.fillAvailable
				flexGrow = 1.0
				overflow = Overflow.auto
			}
			jmTreeItem(
				label = createLabel(Translations.getString("graph.desktop.name")),
				nodeId = nextNodeId(),
				icon = createDesktopIcon(),
				onLabelClick = { props.controller.selectedItem = null }
			) {
				props.controller.project?.let {
					addItems(LibraryDirectoryTreeModelBuilder(it).build())
				}
				addItems(LibraryDirectoryTreeModelBuilder(props.controller.library).build())
			}
		}
	}

	override fun componentDidMount() {
		props.controller.view = this
		props.controller.selectedItem = null
	}

	/** ---- [LibraryTreeView] */

	override fun dispose() { }

	// TODO
	override val folderOfSelectedItem: LibraryDirectory? get() = null

	override fun refresh() {
		forceUpdate()
	}

	override fun expandTo(element: ContainerLibraryElement) {
		// TODO
		LOG.warn("expandTo not yet implemented")
	}

	override fun expandAllFromSelection() {
		// TODO
		LOG.warn("expandAllFromSelection not yet implemented")
	}

	override fun collapseAtSelection() {
		// TODO
		LOG.warn("collapseAtSelection not yet implemented")
	}

	override fun openLibrary(library: Library) {
		refresh()
	}

	override fun openProject(project: Project) {
		refresh()
	}

	override fun closeProject() {
		refresh()
	}

	// TODO?
	override fun handle(event: LibraryItemAddedEvent) {
		refresh()
	}

	// TODO?
	override fun handle(event: LibraryItemRemovedEvent) {
		refresh()
	}

	// TODO?
	override fun handle(event: LibraryItemUpdatedEvent) {
		refresh()
	}

	// TODO?
	override fun handle(event: LibraryItemMovedEvent) {
		refresh()
	}

	// TODO?
	override fun handle(event: LibraryDirectoryRenamedEvent) {
		refresh()
	}

	/** ---- [LibraryTreeViewJs] */

	private fun nextNodeId(): String = (nodeId++).toString()

	private fun RBuilder.addItems(node: LibraryTreeNode) {
		jmTreeItem(
			label = createLabel(node.item.toString()),
			nodeId = nextNodeId(),
			icon = getIcon(node.item),
			onLabelClick = { onLabelClick(it, node) },
			onDoubleClick = props.onDoubleClick?.let {
				handler -> {
					handler.invoke(node)
					it.preventDefault()
				}
			},
			onDragStart = if (node.item is LibraryElement) { { onDragStart(it, node.item) } } else null,
			onDragEnd = if (node.item is LibraryElement) { ::onDragEnd } else null
		) {
			for (node in node.children) {
				addItems(node)
			}
		}
	}

	private fun getIcon(item: LibraryItem): ReactElement? {
		return if (item === props.controller.library) {
			createLibraryIcon()
		} else {
			item.iconPath?.let { IconProviderRegistry.getIcon(it) }
		}
	}

	private fun createExpandIcon(): ReactElement = RBuilder().mIcon("chevron_right")

	private fun createCollapseIcon(): ReactElement = RBuilder().mIcon("expand_more")

	private fun createDesktopIcon(): ReactElement = RBuilder().mIcon("chrome_reader_mode", className = "material-icons-outlined")

	private fun createFolderIcon(): ReactElement = RBuilder().mIcon("folder", className = "material-icons-outlined")

	private fun createProjectIcon(): ReactElement = RBuilder().mIcon("assignment", className = "material-icons-outlined")

	private fun createLibraryIcon(): ReactElement = RBuilder().mIcon("local_library", className = "material-icons-outlined")

	private fun createLabel(text: String): ReactElement = RBuilder().mTypography(text)

	private fun onLabelClick(event: MouseEvent, node: LibraryTreeNode) {
		props.controller.selectedItem = node.item
		if (node.item !is LibraryDirectory) {
			event.preventDefault()
		}
	}

	private fun onDragStart(event: DragEvent, item: LibraryElement) {
		DragAndDropDepo.set(GraphElementViewTransferableData(item.getNewInstance(), item))
		event.dataTransfer?.setData("text/plain", DragAndDropDepo.ID)
		event.dataTransfer?.dropEffect = "copy"

		ensureEmptyDndImage()

		event.dataTransfer?.setDragImage(emptyDragImage!!, 0, 0)
	}

	private fun onDragEnd(event: DragEvent) {
		DragAndDropDepo.clear()
		emptyDragImage?.let {
			document.body?.removeChild(it)
		}
	}

	private fun ensureEmptyDndImage() {
		if (emptyDragImage == null) {
			emptyDragImage = (document.createElement("canvas") as HTMLCanvasElement).apply {
				width = 1
				height = 1
			}
		}
		document.body?.appendChild(emptyDragImage!!)
	}
}