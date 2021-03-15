package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import org.apache.commons.io.IOUtils
import java.net.URL
import java.nio.charset.StandardCharsets

interface ApplicationVersionService {

	/**
	 * Makes a remote call to check if there is a newer [ApplicationVersion] than the currently
	 * used one available, and returns the newer [ApplicationVersion] if the current user
	 * has not wished to ignore that newer version.
	 *
	 * @return the newest [ApplicationVersion] to be offered to the user, or `null` if
	 * there is no newer version or the user wished to ignore that newer version.
	 */
	fun checkForNewerVersion(currentVersion: ApplicationVersion): ApplicationVersion?

	/** Marks the specified version as to be ignored by the user.*/
	fun ignoreNewVersion(newVersion: ApplicationVersion)
}

class ApplicationVersionServiceImpl(
	private val properties: Properties = BaseModule.properties,
	private val newVersionReader: (String) -> String = { IOUtils.toString(URL(it), StandardCharsets.UTF_8) }
) : ApplicationVersionService {

	companion object {

		private val LOG by logger(ApplicationVersionServiceImpl::class)

		/** The name of the optional [String] property in [Properties] of the remote URL containing the newest version name. */
		const val PROP_VERSION_FILE_URL = "ch.scorpion.jabbah.app.ApplicationVersionService.versionUrl"

		/** The name of the [String] property in [Settings] denoting the new app version the user wants to ignore.*/
		const val PROP_IGNORED_VERSION = "ch.scorpion.jabbah.app.ApplicationVersionService.ignoredVersion"
	}

	override fun checkForNewerVersion(currentVersion: ApplicationVersion): ApplicationVersion? {
		val ignoredVersion = readIgnoredVersion()
		val newestVersion = retrieveNewestVersion()
		if (newestVersion != null && newestVersion > currentVersion && newestVersion != ignoredVersion) {
			return newestVersion
		}
		return null
	}

	override fun ignoreNewVersion(newVersion: ApplicationVersion) {
		LOG.info("Marking version $newVersion to be ignored by user")
		properties.customize(PROP_IGNORED_VERSION, newVersion)
	}

	private fun readIgnoredVersion(): ApplicationVersion {
		val versionText = properties.getString(PROP_IGNORED_VERSION)
		return ApplicationVersion(versionText)
	}

	private fun retrieveNewestVersion(): ApplicationVersion? {
		val urlParam: String? = properties.getOptional(PROP_VERSION_FILE_URL)
		if (urlParam != null) {
			return try {
				ApplicationVersion(newVersionReader(urlParam))
			} catch (e: Throwable) {
				LOG.error("Error while fetching newest version number from $urlParam: ${e.message}")
				null
			} catch (e: IllegalArgumentException) {
				LOG.error("Error while parsing newest version number: ${e.message}")
				null
			}
		}

		return null
	}
}