package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.ActionEvent

class YouTubeChannelAction(
	application: Application
) : AbstractApplicationAction("help.action.youtubeChannel", application) {

	init {
		enabled = application.documentationUrl != null
	}

	override fun execute(event: ActionEvent) {
		System.browse("${application.youtubeChannelUrl!!}", name)
	}
}