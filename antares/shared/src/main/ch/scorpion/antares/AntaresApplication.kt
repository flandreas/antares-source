package ch.scorpion.antares

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.UUID

interface AntaresApplication : Application {

	companion object {
		const val SYSTEM_NAME = "Antares"
		const val DISPLAY_NAME = "Antares"
		const val DEFAULT_LIB_DIRECTORY = "libraries"
		const val DEFAULT_PROJECT_DIRECTORY = "projects"
		const val DEFAULT_LIB_FILENAME = "library.xml"
		const val FILE_EXTENSION_NAME = "cir"
		const val DOC_URL_PROD = "https://www.antarescircuit.io/user-manual/english"
		const val DOC_URL_DEV = "http://127.0.0.1:4000/user-manual/english"
		const val ISSUES_URL_PROD = "https://www.antarescircuit.io/docs/issues"
		const val ISSUES_URL_DEV = "http://127.0.0.1:4000/docs/issues"
		const val YOUTUBE_CHANNEL_URL = "https://www.youtube.com/channel/UCFs7EUOuFBXcnFLSlRd8Q0w"
		val DEF_LIBRARY_UUID = UUID("6707f981-110d-4629-a0bf-c35a4688025c")
		const val FREQUENTLY_USED_FOLDER_NAME_EN = "Frequently used"
	}

	override val displayName: String get() = DISPLAY_NAME

	override val systemName: String get() = SYSTEM_NAME

}