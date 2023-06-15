package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import org.apache.commons.cli.*
import org.apache.commons.io.IOUtils
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
		 * The name of the [System] property that contains the absolute path of the log file.
		 * Set during start-up and used by the log4j configuration as well as the [Action] for exporting
		 * the log file.
		 */
		private const val PROP_LOGFILE_PATH = "system.logFile"

		/** The command line option for the application's data directory.*/
		private const val APP_DATA_DIR_OPTION = "d"

		/**
		 * The command line option for the user's data directory. By default, this is the same as [APP_DATA_DIR_OPTION],
		 * but can be set to something else to support various "Workspaces".
		 */
		private const val USER_DATA_DIR_OPTION = "ud"

		/** The command line option for activating developers mode. */
		private const val DEVELOPER_OPTION = "dev"

		/** The command line option for specifying the [Environment] in which the application runs. */
		private const val ENVIRONMENT_OPTION = "env"

		const val SETTINGS_FILE_EXTENSION = "ini"

		const val PREFERENCES_FILE_EXTENSION = "pref"

		/** Defines the command line argument [Options] for this [DesktopApplication].*/
		fun defineOptions(options: Options): Options {
			options.addOption(Option.builder(APP_DATA_DIR_OPTION)
				.required(false)
				.longOpt("appDir")
				.desc("Application data directory")
				.hasArg()
				.build())

			options.addOption(Option.builder(USER_DATA_DIR_OPTION)
				.required(false)
				.longOpt("userDir")
				.desc("User data directory")
				.hasArg()
				.build())

			options.addOption(Option.builder(DEVELOPER_OPTION)
				.required(false)
				.longOpt("developer")
				.desc("Run in developer mode")
				.hasArg(false)
				.build())

			options.addOption(Option.builder(ENVIRONMENT_OPTION)
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

		fun determineAppDataDirectoryPath(commandLine: CommandLine, systemName: String): Path {
			val path = if (commandLine.hasOption(APP_DATA_DIR_OPTION)) {
				FileSystems.getDefault().getPath(commandLine.getOptionValue(APP_DATA_DIR_OPTION))
			} else {
				FileSystems.getDefault().getPath(getDefaultAppDataDirectory(), systemName)
			}
			val absolutePath = path.toAbsolutePath().toString()
			System.setProperty(PROP_LOGFILE_PATH, Paths.get(absolutePath, calculateLogfileName(systemName)).toString())
			return path
		}

		private fun determineEnvironment(commandLine: CommandLine): Environment {
			if (!commandLine.hasOption(ENVIRONMENT_OPTION)) {
				return Environment.Production
			}
			try {
				return Environment.withName(commandLine.getOptionValue(ENVIRONMENT_OPTION))
			} catch (e: IllegalArgumentException) {
				System.err.println(e.message)
				exitProcess(1)
			}
		}

		private fun getDefaultAppDataDirectory(): String =
			when {
				SystemUtils.IS_OS_MAC -> System.getProperty("user.home") + "/Library/Application Support"
				SystemUtils.IS_OS_WINDOWS -> System.getenv("APPDATA")
				SystemUtils.IS_OS_UNIX -> System.getProperty("user.home")
				else -> System.getProperty("user.dir")
			}

		private fun calculateLogfileName(systemName: String): String = "$systemName.log"

		fun readVersion(): ApplicationVersion = ApplicationVersion.parse(
			IOUtils.toString(this::class.java.getResourceAsStream("/version.txt"), "UTF-8"))
	}

	// Must not be in companion object due to Module and LogSystem bootstrapping order
	@Suppress("PrivatePropertyName")
	private val LOG by logger(AbstractDesktopApplication::class)

	private val logfileName: String get() = calculateLogfileName(systemName)

	override val appDataDirectoryPath: Path = determineAppDataDirectoryPath(commandLine, systemName)

	init {
		LOG.info("Starting $displayName version ${readVersion()}")
		LOG.info("Using Java ${Runtime.version()}")
		LOG.info(("Using app data directory $appDataDirectoryPath"))

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

	override val version: ApplicationVersion by lazy { readVersion() }

	override fun quit(): Boolean {
		if (controller.canReplaceSavable("file.action.quit.name")) {
			shutdown()
			return true
		}
		return false
	}

	override fun exportLogfile(destinationPath: String) {
		LOG.userTrail("Exporting log file to $destinationPath")
		FileOutputStream(destinationPath).use { output ->
			ZipOutputStream(output).use {
				val fileToZip = File(Paths.get(appDataDirectoryPath.toAbsolutePath().toString(), logfileName).toUri())
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
	 * Subclasses can overwrite this method in order to consume and use the provided [Options].
	 */
	protected open fun consumeCommandLine(commandLine: CommandLine) {
		consumeDeveloperOption(commandLine)
		determineUserDataDirectoryPath(commandLine)
	}

	private fun consumeDeveloperOption(commandLine: CommandLine) {
		EditAuthModule.userHolder = if (commandLine.hasOption(DEVELOPER_OPTION)) {
			LOG.info("Running application in developer mode")
			DesktopUserHolder(DesktopUser.developer)
		} else {
			DesktopUserHolder(DesktopUser.anybody)
		}
	}

	private fun determineUserDataDirectoryPath(commandLine: CommandLine) {
		val path = if (commandLine.hasOption(USER_DATA_DIR_OPTION)) {
			FileSystems.getDefault().getPath(commandLine.getOptionValue(USER_DATA_DIR_OPTION))
		} else {
			determineAppDataDirectoryPath(commandLine, systemName)
		}
		AppModuleJvm.workspaceHolder = WorkspaceHolder(Workspace(path.toAbsolutePath().toString()))
	}

	private fun getSettingsPath(): Path =
		FileSystems.getDefault().getPath(appDataDirectoryPath.toString(), "$systemName.$SETTINGS_FILE_EXTENSION")

	private fun getPreferencesPath(): Path =
		FileSystems.getDefault().getPath(appDataDirectoryPath.toString(), "$systemName.$PREFERENCES_FILE_EXTENSION")

	/** Ensures that the application's data directory exists by creating it if it doesn't. */
	private fun ensureAppDataDirectory() {
		val path = appDataDirectoryPath
		if (Files.notExists(path)) {
			LOG.userTrail("Creating app data directory '$path'")
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
		ensureAppDataDirectory()
		FileOutputStream(path.toString()).use {
			try {
				//val properties = java.util.Properties()
				val properties = SortedProperties()
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
		ensureAppDataDirectory()
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