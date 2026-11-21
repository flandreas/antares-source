package io.antarescircuit.antares.web

import io.antarescircuit.antares.view.AntaresFrameController
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController

/**
 * What feature modules of the web editor (memory, truth tables, …) need from [AntaresWebEditor].
 * Feature classes receive it in their constructor and are exposed to the web application as properties
 * of [AntaresWebEditor].
 */
class WebEditorContext internal constructor(
	internal val dataController: () -> GraphDataViewController,
	internal val frame: () -> AntaresFrameController,
	internal val library: () -> WebLibrary,
	/** Schedules a change notification to the web application. */
	internal val notifyChanged: () -> Unit,
	/** Shows a message: (type, title, text) with type info|warning|error|success. */
	internal val message: (String, String, String) -> Unit,
	/** Saves the open document if it has changes. */
	internal val saveNow: () -> Unit,
	/** Opens a project item by UUID (circuits in the editor, other items as documents). */
	internal val openItem: (String) -> Unit,
	/** The document shown instead of the circuit editor, if any. */
	internal val document: () -> WebDocumentItem?,
	/** The document shown in a dialog, if any. */
	internal val dialog: () -> WebDocumentItem?,
	/** Runs [block] reporting exceptions to the user, returning [fallback] on failure. */
	internal val guardedAny: (fallback: Any?, block: () -> Any?) -> Any?,
) {
	internal val editor: Editor get() = frame().editor
	/** The open project, or the open user library while a library is edited (see [WebLibraries]). */
	internal val project: Library? get() = LibraryModule.libraryHolder.l

	@Suppress("UNCHECKED_CAST")
	internal fun <T> guarded(fallback: T, block: () -> T): T = guardedAny(fallback, block) as T
}
