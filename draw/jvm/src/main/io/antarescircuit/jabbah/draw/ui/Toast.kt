package io.antarescircuit.jabbah.draw.ui

import io.antarescircuit.jabbah.animation.Animator

/** Desktop rendering is installed by DrawModuleUiJvm. Core has no toast surface. */
actual object Toast {

	var handler: (String, Animator) -> Unit = { _, _ -> }

	actual fun show(message: String, animator: Animator) {
		handler(message, animator)
	}
}
