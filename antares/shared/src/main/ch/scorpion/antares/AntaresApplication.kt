package ch.scorpion.antares

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.UUID

interface AntaresApplication : Application {

	companion object {
		const val SYSTEM_NAME = "Antares"
		const val DISPLAY_NAME = "Antares"
		const val DEFAULT_LIB_DIRECTORY = "libraries"
		const val DEFAULT_PROJECT_DIRECTORY = "projects"
		const val DEFAULT_NON_VOLATILE_DIRECTORY = "nonVolatile"
		const val DEFAULT_LIB_FILENAME = "library.xml"
		const val FILE_EXTENSION_NAME = "cir"
		const val DOC_URL_PROD = "https://www.antarescircuit.io/user-manual/english"
		const val DOC_URL_DEV = "http://127.0.0.1:4000/user-manual/english"
		const val ISSUES_URL_PROD = "https://www.antarescircuit.io/docs/issues"
		const val ISSUES_URL_DEV = "http://127.0.0.1:4000/docs/issues"
		const val YOUTUBE_CHANNEL_URL = "https://www.youtube.com/channel/UCFs7EUOuFBXcnFLSlRd8Q0w"
		val DEF_LIBRARY_UUID = UUID("6707f981-110d-4629-a0bf-c35a4688025c")
		const val FREQUENTLY_USED_FOLDER_NAME_EN = "Frequently used"

		const val AKRAB_DEV_URL = "http://localhost:8080/api"
		const val AKRAB_PROD_URL = "https://api.antarescircuit.io/api"

		const val ANTARES_VIEWER_JS_URL = "https://viewer.antarescircuit.io"

		val DIGITAL_LIBRARY_IDS = listOf(
			"cb21300b-8f5d-4c64-8f37-5d9a49807e8c",
			"6707f981-110d-4629-a0bf-c35a4688025c")

		val ANALOG_LIBRARY_IDS = listOf(
			"b3a5e306-f0c8-11ed-a05b-0242ac120003")
	}

	override val displayName: String get() = DISPLAY_NAME

	override val systemName: String get() = SYSTEM_NAME

}