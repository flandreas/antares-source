package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent

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