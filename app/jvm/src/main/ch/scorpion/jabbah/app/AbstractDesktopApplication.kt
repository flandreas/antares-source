package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import org.apache.commons.cli.*
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.System
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/** Abstract base implementation of the [DesktopApplication] interface. */
abstract class AbstractDesktopApplication(
	protected val commandLine: CommandLine,
	controller: ApplicationDataViewController
) : AbstractApplication(controller), DesktopApplication {

	companion object {

		/**
		 * The name of the [System] property than contains the absolute user data directory path.
		 * Set during start-up and used by the log4j configuration in order to write the log file
		 * in the user's data directory.
		 */
		private const val PROP_USER_DATA_DIRECTORY = "user.dataDirectory"

		/**
		 * The name of the [System] property that contains the absolute path of the log file.
		 * Set during start-up and used by the log4j configuration as well as the [Action] for exporting
		 * the log file.
		 */
		private const val PROP_LOGFILE_PATH = "system.logFile"

		const val SETTINGS_FILE_EXTENSION = "ini"

		const val PREFERENCES_FILE_EXTENSION = "pref"

		/** Defines the command line argument [Options] for this [DesktopApplication].*/
		fun defineOptions(options: Options): Options {
			options.addOption(Option.builder("d")
				.required(false)
				.longOpt("directory")
				.desc("Home directory")
				.hasArg()
				.build())

			options.addOption(Option.builder("dev")
				.required(false)
				.longOpt("developer")
				.desc("Run in developer mode")
				.hasArg(false)
				.build())

			options.addOption(Option.builder("env")
				.required(false)
				.longOpt("environment")
				.desc("Run environment")
				.hasArg()
				.build())

			return options
		}

		fun parseCommandLine(args: Array<String>, options: Options, programName: String): CommandLine {
			try {
				return DefaultParser().parse(options, args)!!
			} catch (x: ParseException) {
				System.err.println("Error while parsing options: ${x.message}")
				HelpFormatter().printHelp(programName, options)
				exitProcess(1)
			}
		}

		fun determineUserDataDirectoryPath(commandLine: CommandLine, systemName: String): Path {
			val path = if (commandLine.hasOption("d")) {
				FileSystems.getDefault().getPath(commandLine.getOptionValue("d"))
			} else {
				FileSystems.getDefault().getPath(getDefaultUserDataDirectory(), systemName)
			}
			val absolutePath = path.toAbsolutePath().toString()
			System.setProperty(PROP_USER_DATA_DIRECTORY, absolutePath)
			System.setProperty(PROP_LOGFILE_PATH, Paths.get(absolutePath, calculateLogfileName(systemName)).toString())
			return path
		}

		private fun determineEnvironment(commandLine: CommandLine): Environment {
			if (!commandLine.hasOption("env")) {
				return Environment.Production
			}
			try {
				return Environment.withName(commandLine.getOptionValue("env"))
			} catch (e: IllegalArgumentException) {
				System.err.println(e.message)
				exitProcess(1)
			}
		}

		private fun getDefaultUserDataDirectory(): String {
			return when {
				SystemUtils.IS_OS_MAC -> System.getProperty("user.home") + "/Library/Application Support"
				SystemUtils.IS_OS_WINDOWS -> System.getenv("APPDATA")
				SystemUtils.IS_OS_UNIX -> System.getProperty("user.home")
				else -> System.getProperty("user.dir")
			}
		}

		private fun calculateLogfileName(systemName: String): String = "$systemName.log"
	}

	// Must not be in companion object due to Module and LogSystem bootstrapping order
	@Suppress("PrivatePropertyName")
	private val LOG by logger(AbstractDesktopApplication::class)

	private val logfileName: String get() = calculateLogfileName(systemName)

	override val userDataDirectoryPath: Path = determineUserDataDirectoryPath(commandLine, systemName)

	init {
		LOG.info("Starting $displayName version ${readVersion()}")
		LOG.info(("Using user data dictionary $userDataDirectoryPath"))
		consumeCommandLine(commandLine)
		loadSettings()
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		super.init()
		LOG.info("Running in environment '$environment'")
		loadPreferences()
		LogSystem.level = LogLevel.valueOf(BaseModule.properties.getString(LogSystem.PROP_LOG_LEVEL))
		if (Translations.language.code != BaseModule.properties.getString(Language.PROP_LANGUAGE)) {
			Translations.language = Language.withCode(BaseModule.properties.getString(Language.PROP_LANGUAGE))
		}
	}

	/** ---- [DesktopApplication] */

	override val environment: Environment = determineEnvironment(commandLine)

	override fun quit() {
		if (controller.canReplaceSavable("file.action.quit.name")) {
			shutdown()
		}
	}

	override fun exportLogfile(destinationPath: String) {
		LOG.userTrail("Exporting log file to $destinationPath")
		FileOutputStream(destinationPath).use { output ->
			ZipOutputStream(output).use {
				val fileToZip = File(Paths.get(userDataDirectoryPath.toAbsolutePath().toString(), logfileName).toUri())
				ZipUtil.zipFile(fileToZip, fileToZip.name, it)
			}
		}
	}

	/** ---- [AbstractDesktopApplication] */

	protected abstract fun shutdownUI()

	/**
	 * Called by [AbstractDesktopApplication.shutdown] before the settings are stored.
	 * This implementation is empty. Subclasses can overwrite this method e.g. to store application level settings.
	 */
	protected open fun handleShutdown() {
		// empty
	}

	protected open fun shutdown() {
		LOG.info("Shutting $displayName down")
		handleShutdown()
		shutdownUI()
		storePreferences()
		storeSettings()
		exitProcess(0)
	}

	/**
	 * Called by this [AbstractDesktopApplication] after the options have been parsed.
	 * This implementation does nothing. Subclasses can overwrite this method in order to consume and use
	 * the provided [Options].
	 */
	protected open fun consumeCommandLine(commandLine: CommandLine) {
		EditAuthModule.userHolder = if (commandLine.hasOption("dev")) {
			LOG.info("Starting application in developer mode")
			DesktopUserHolder(DesktopUser.developer)
		} else {
			DesktopUserHolder(DesktopUser.anybody)
		}
	}

	private fun getSettingsPath(): Path {
		return FileSystems.getDefault().getPath(userDataDirectoryPath.toString(), "$systemName.$SETTINGS_FILE_EXTENSION")
	}

	private fun getPreferencesPath(): Path {
		return FileSystems.getDefault().getPath(userDataDirectoryPath.toString(), "$systemName.$PREFERENCES_FILE_EXTENSION")
	}

	/** Ensures that the user's data directory for this application exists by creating it if it doesn't. */
	private fun ensureUserDataDirectory() {
		val path = userDataDirectoryPath
		if (Files.notExists(path)) {
			LOG.userTrail("Creating home directory '$path'")
			Files.createDirectories(path)
		}
	}

	private fun loadSettings() {
		val path = getSettingsPath()
		LOG.debug("Loading settings from '$path'")
		try {
			FileInputStream(path.toString()).use {
				try {
					val settings = java.util.Properties()
					settings.load(it)
					for (key in settings.keys) {
						BaseModule.settings.set(key as String, settings[key]!!)
					}
				} catch (x: Throwable) {
					LOG.error("Error while loading settings: ${x.message}")
				}
			}
		} catch (x: FileNotFoundException) {
			// empty
		}
	}

	private fun storeSettings() {
		val path = getSettingsPath()
		LOG.debug("Storing settings in $path")
		ensureUserDataDirectory()
		FileOutputStream(path.toString()).use {
			try {
				val properties = java.util.Properties()
				for (key in BaseModule.settings.getKeys()) {
					properties.setProperty(key, BaseModule.settings.get(key))
				}
				properties.store(it, null)
			} catch (x: Throwable) {
				LOG.error("Error while storing settings: ${x.message}")
			}
		}
	}

	private fun loadPreferences() {
		val path = getPreferencesPath()
		LOG.debug("Loading preferences from '$path'")
		try {
			FileInputStream(path.toString()).use {
				try {
					val preferences = java.util.Properties()
					preferences.load(it)
					for (key in preferences.keys) {
						BaseModule.properties.load(key as String, preferences.getProperty(key))
					}
					controller.eventBus.post(PreferencesChangedEvent(BaseModule.properties))
				} catch (x: Throwable) {
					LOG.error("Error while loading preferences: ${x.message}")
				}
			}
		} catch (x: FileNotFoundException) {
			// empty
		}
	}

	private fun storePreferences() {
		val path = getPreferencesPath()
		LOG.debug("Storing preferences in $path")
		ensureUserDataDirectory()
		FileOutputStream(path.toString()).use {
			try {
				val preferences = java.util.Properties()
				for (key in BaseModule.properties.getCustomizedKeys()) {
					preferences[key] = BaseModule.properties.getEntry(key).stringValue
				}
				preferences.store(it, null)
			} catch (x: Throwable) {
				LOG.error("Error while storing preferences: ${x.message}")
			}
		}
	}
}