package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import org.apache.commons.io.IOUtils
import java.io.StringReader
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Fetches a properties file from a remote location and provides property values
 * to be used for controlling a [DesktopApplication] from remote.
 * Properties include things like "current version number".
 */
class RemoteControlService(
	private val configProperties: Properties = BaseModule.properties,
	private val contentReader: (String) -> String = { IOUtils.toString(URL(it), StandardCharsets.UTF_8) }
) {

	companion object {

		private val LOG by logger(RemoteControlService::class)

		/** The name of the property in the remote file containing the current version number.*/
		private const val REMOTE_PROP_VERSION = "app.currentVersion"

		/** The name of the optional [String] property in [Properties] of the URL containing the remote properties.*/
		//const val PROP_PROPERTIES_FILE_URL = "RemoteControlService.fileUrl"

		/**
		 * The name of the [String] property in [Settings] denoting the new app version the user wants to ignore.
		 * Still use to name of the legacy service for backward compatibility.
		 */
		const val PROP_IGNORED_VERSION = "ch.scorpion.jabbah.app.ApplicationVersionService.ignoredVersion"
	}

	private val remoteProperties = lazy { loadRemoteProperties() }

	/**
	 * Makes a remote call to check if there is a newer [ApplicationVersion] than the currently
	 * used one available, and returns the newer [ApplicationVersion] if the current user
	 * has not wished to ignore that newer version.
	 *
	 * @return the newest [ApplicationVersion] to be offered to the user, or `null` if
	 * there is no newer version or the user wished to ignore that newer version.
	 */
	fun checkForNewerVersion(currentVersion: ApplicationVersion): ApplicationVersion? {
		val ignoredVersion = readIgnoredVersion()
		val newestVersion = retrieveNewestVersion()
		if (newestVersion != null && newestVersion > currentVersion && newestVersion != ignoredVersion) {
			return newestVersion
		}
		return null
	}

	/** Marks the specified version as to be ignored by the user.*/
	fun ignoreNewVersion(newVersion: ApplicationVersion) {
		LOG.info("Marking version $newVersion to be ignored by user")
		configProperties.customize(PROP_IGNORED_VERSION, newVersion)
	}

	/** Returns the boolean value of the remote property with [name].*/
	fun getBoolean(name: String, default: Boolean = false): Boolean
		= remoteProperties.value?.getBoolean(name) ?: default

	private fun readIgnoredVersion(): ApplicationVersion = ApplicationVersion(configProperties.getString(PROP_IGNORED_VERSION))

	private fun retrieveNewestVersion(): ApplicationVersion? = remoteProperties.value?.let {
		ApplicationVersion(it.getString(REMOTE_PROP_VERSION))
	}

	private fun loadRemoteProperties(): Properties? {
		val urlParam: String? = AppModuleJvm.remotePropertiesUrl
		return if (urlParam != null) {
			try {
				val jp = java.util.Properties()
				jp.load(StringReader(contentReader(urlParam)))
				val p = Properties()
				for (key in jp.keys) {
					p.load(key as String, jp.getProperty(key))
				}
				p
			} catch (e: Throwable) {
				LOG.error("Error while fetching remote properties from $urlParam: ${e.message}")
				null
			}
		} else {
			null
		}
	}
}