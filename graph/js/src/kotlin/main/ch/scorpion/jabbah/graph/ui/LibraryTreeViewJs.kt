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

external interface LibraryTreeViewJsProps : Props {
	var application: Application
	var controller: LibraryTreeViewController
	var onDoubleClick: (node: LibraryTreeNode) -> Unit
}

fun RBuilder.libraryTreeView(handler: LibraryTreeViewJsProps.() -> Unit) {
	child(LibraryTreeViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryTreeViewJs(
	props: LibraryTreeViewJsProps
) : RComponent<LibraryTreeViewJsProps, State>(props), LibraryTreeView {

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
			defaultExpandIcon = buildElement { mIcon("chevron_right") },
			defaultCollapseIcon = buildElement { mIcon("expand_more") },
		) {
			css {
				paddingLeft = 2.spacingUnits
				maxHeight = LinearDimension.fillAvailable
				maxWidth = LinearDimension.fillAvailable
				flexGrow = 1.0
				overflow = Overflow.auto
			}
			jmTreeItem(
				label = buildElement { mTypography(Translations.getString("graph.desktop.name")) },
				nodeId = nextNodeId(),
				icon = buildElement { mIcon("table_restaurant", className = "material-icons-outlined") },
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

	// TODO?
	override fun handle(event: ContainerLibraryElementRenamedEvent) {
		refresh()
	}

	/** ---- [LibraryTreeViewJs] */

	private fun nextNodeId(): String = (nodeId++).toString()

	private fun RBuilder.addItems(node: LibraryTreeNode) {
		jmTreeItem(
			label = buildElement { mTypography(node.item.toString()) },
			nodeId = nextNodeId(),
			icon = if (node.item === props.controller.library) {
				buildElement { mIcon("account_balance", className = "material-icons-outlined") }
			} else if (node.item === props.controller.project) {
				buildElement { mIcon("assignment", className = "material-icons-outlined") }
			} else {
				if (node.item is LibraryFolder) {
					buildElement { mIcon("folder", className = "material-icons-outlined") }
				} else if (node.item.iconPath == null) {
					buildElement { mIcon("fiber_manual_record", className = "material-icons-outlined") }
				} else {
					node.item.iconPath?.let { IconProviderRegistry.getIcon(it) }
				}
			},
			onLabelClick = { onLabelClick(it, node) },
			onDoubleClick = props.onDoubleClick.let {
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