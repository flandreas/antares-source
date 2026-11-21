package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.fsm.FSMLibraryItem
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.view.AntaresFrame
import io.antarescircuit.antares.view.addressable.OpenMemoryContentsRequest
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryItem
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/** Kinds of project items, as used by the web application. */
internal object WebItemKinds {
	const val CIRCUIT = "circuit"
	const val TRUTH_TABLE = "truthTable"
	const val FSM = "fsm"
	const val MEMORY = "memory"
	const val EXPRESSION = "expression"
	const val IMAGE = "image"
	const val MEMORY_CONTENTS = "memoryContents"
	const val DOCUMENTATION = "documentation"

	fun kindOf(item: LibraryItem): String? = when (item) {
		is ContainerLibraryElement -> CIRCUIT
		is TruthTableLibraryItem -> TRUTH_TABLE
		is FSMLibraryItem -> FSM
		is MemoryLibraryItem -> MEMORY
		is BooleanExpressionLibraryItem -> EXPRESSION
		is ImageLibraryElement -> IMAGE
		else -> null
	}

	fun uuidOf(item: LibraryItem): String? = when (item) {
		is ContainerLibraryElement -> item.uuid.id
		is TruthTableLibraryItem -> item.uuid.id
		is FSMLibraryItem -> item.uuid.id
		is MemoryLibraryItem -> item.uuid.id
		is BooleanExpressionLibraryItem -> item.uuid.id
		is ImageLibraryElement -> item.storable.uuid.id
		else -> null
	}
}

/**
 * A document shown by the web application instead of the circuit canvas (e.g. a truth table),
 * or in a dialog (memory contents, documentation of a sub-circuit).
 *
 * @param kind one of [WebItemKinds]
 * @param payload the shared model object, e.g. a [TruthTableLibraryItem] or an [OpenMemoryContentsRequest]
 */
class WebDocumentItem internal constructor(
	val kind: String,
	val payload: Any,
) : GraphDesktopViewItem {
	override val reusable: Boolean get() = false
	override val drawingView: DrawingView<GraphElementView<*>, GraphView>? get() = null
	override var contextColor: CompositeColor? = null
	override val isDetached: Boolean get() = false
	override val layoutWidth: Int get() = 0
	override val layoutHeight: Int get() = 0
	override fun disposeItem() {}
	override fun findContent(condition: (DrawingViewContent<GraphElementView<*>, GraphView>) -> Boolean): DrawingViewContent<*, *>? = null
	override fun createCloseRequest(): Any = "Close Request"
	override fun displays(content: Any?): Boolean = when (val p = payload) {
		is TruthTableLibraryItem -> content === p.storable
		is FSMLibraryItem -> content === p.storable
		is MemoryLibraryItem -> content === p.storable
		is BooleanExpressionLibraryItem -> content === p.storable
		is ImageLibraryElement -> content === p.storable
		else -> content === p
	}
	override val mainUI: Any? get() = null
	override val view: View<out EditInputEventContext>? get() = null
}

/** Documentation of a sub-circuit, opened from its context menu. */
internal class WebDocumentation(val title: String, val document: Document)

/**
 * The browser implementation of [AntaresFrame]: library items and other documents are reported to the
 * web application as [WebDocumentItem]s.
 */
internal class WebAntaresFrame(
	controller: () -> GraphFrameController<*>,
	onDisplayedViewChanged: () -> Unit,
	private val onMemoryContents: (OpenMemoryContentsRequest) -> Unit,
) : AntaresFrame, io.antarescircuit.jabbah.graph.ui.GraphFrame by WebGraphFrame(controller, onDisplayedViewChanged) {

	override fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor) =
		WebDocumentItem(WebItemKinds.MEMORY_CONTENTS, request)

	override fun createTruthTableDesktopViewItem(item: TruthTableLibraryItem) = WebDocumentItem(WebItemKinds.TRUTH_TABLE, item)

	override fun createBooleanExpressionDesktopViewItem(item: BooleanExpressionLibraryItem) =
		WebDocumentItem(WebItemKinds.EXPRESSION, item)

	override fun createMemoryStorableGraphDesktopViewItem(item: MemoryLibraryItem) = WebDocumentItem(WebItemKinds.MEMORY, item)

	override fun createImageGraphDesktopViewItem(element: ImageLibraryElement) = WebDocumentItem(WebItemKinds.IMAGE, element)

	override fun createFSMDesktopViewItem(item: FSMLibraryItem) = WebDocumentItem(WebItemKinds.FSM, item)

	override fun createDocumentationDesktopViewItem(documentation: Document, metaGraphName: String) =
		WebDocumentItem(WebItemKinds.DOCUMENTATION, WebDocumentation(metaGraphName, documentation))

	override fun showMemoryContents(request: OpenMemoryContentsRequest) = onMemoryContents(request)
}
