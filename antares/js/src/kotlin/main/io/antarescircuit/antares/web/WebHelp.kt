package io.antarescircuit.antares.web

import io.antarescircuit.antares.AntaresApplication
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.help.HelpIdProvider
import io.antarescircuit.jabbah.base.help.HelpSourceRegistry

/**
 * Help pages of the online user manual (desktop: `HelpComponentAction`, `HelpLibraryItemAction` and
 * `DocumentationAction`, which open the pages registered in [HelpSourceRegistry] in a browser).
 */
@JsExport
class WebHelp internal constructor(private val ctx: WebEditorContext) {

	/** The start page of the user manual. */
	fun manualUrl(): String = "${AntaresApplication.DOC_URL_PROD}/usermanual"

	/** The help page of the single selected component, or `null` if there is none. */
	fun selectionUrl(): String? = ctx.guarded(null) {
		if (ctx.document() != null) return@guarded null
		val selection = ctx.editor.view.selectionManager.selection
		(selection.singleOrNull() as? HelpIdProvider)?.helpId?.let { urlOf(it) }
	}

	/** The help page of the palette element with [key], or `null` if there is none (e.g. for project circuits). */
	fun paletteUrl(key: String): String? = ctx.guarded(null) {
		(ctx.library().element(key) as? HelpIdProvider)?.helpId?.let { urlOf(it) }
	}

	private fun urlOf(helpId: HelpId): String? =
		HelpSourceRegistry.getHelpSource(helpId)?.let { source ->
			val path = source.source.let { if (it.startsWith("/")) it else "/$it" }
			"${AntaresApplication.DOC_URL_PROD}$path"
		}
}
