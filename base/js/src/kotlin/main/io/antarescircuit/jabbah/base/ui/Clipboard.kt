package io.antarescircuit.jabbah.base.ui

import kotlinx.browser.window

/**
 * Keeps clipboard contents in memory, because the browser clipboard can only be read asynchronously.
 * Contents are also written to the system clipboard (best effort), and web apps can feed system clipboard
 * contents from `paste` events into [setStringContents] before pasting.
 */
actual object Clipboard {

	private var contents: String? = null

	actual fun getStringContents(): String? = contents

	actual fun setStringContents(contents: String) {
		this.contents = contents
		try {
			window.navigator.asDynamic().clipboard?.writeText(contents)?.catch { _: dynamic -> }
		} catch (_: Throwable) {
			// Clipboard API unavailable, e.g. insecure context
		}
	}

	/** Sets the contents without touching the system clipboard. */
	fun setLocalContents(contents: String) {
		this.contents = contents
	}
}
