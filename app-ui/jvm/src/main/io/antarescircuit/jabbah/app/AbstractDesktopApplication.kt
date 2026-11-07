package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.app.AbstractDesktopApplication.Companion.APP_DATA_DIR_OPTION
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.app.workspace.WorkspacePanel
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.io.ZipUtil
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
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
import kotlin.Array
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.Suppress
import kotlin.Throwable
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.getValue
import kotlin.lazy
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
				determineDefaultUserDataDirectoryPath(systemName)
			}
			val absolutePath = path.toAbsolutePath().toString()
			System.setProperty(PROP_LOGFILE_PATH, Paths.get(absolutePath, calculateLogfileName(systemName)).toString())
			return path
		}

		private fun determineDefaultUserDataDirectoryPath(systemName: String): Path =
            FileSystems.getDefault().getPath(getDefaultAppDataDirectory(), systemName)

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

		fun getDefaultAppDataDirectory(): String =
			when {
				SystemUtils.IS_OS_MAC -> System.getProperty("user.home") + "/Library/Application Support"
				SystemUtils.IS_OS_WINDOWS -> System.getenv("APPDATA")
				SystemUtils.IS_OS_UNIX -> System.getProperty("user.home")
				else -> System.getProperty("user.dir")
			}

		private fun calculateLogfileName(systemName: String): String = "$systemName.log"

		fun readCodeVersion(): ApplicationVersion = ApplicationVersion.parse(
			IOUtils.toString(this::class.java.getResourceAsStream("/codeVersion.txt"), "UTF-8"))

		fun readDataVersion(): ApplicationVersion = ApplicationVersion.parse(
			IOUtils.toString(this::class.java.getResourceAsStream("/dataVersion.txt"), "UTF-8"))

	}

	// Must not be in companion object due to Module and LogSystem bootstrapping order
	@Suppress("PrivatePropertyName")
	private val LOG by logger(AbstractDesktopApplication::class)

	override val logFileName: String get() = calculateLogfileName(systemName)

	override val appDataDirectoryPath: Path = determineAppDataDirectoryPath(commandLine, systemName)

	override val codeVersion: ApplicationVersion by lazy { readCodeVersion() }

	override val dataVersion: ApplicationVersion by lazy { readDataVersion() }

	init {
		LOG.info("Starting $displayName version $codeVersion")
		LOG.info("Running on ${SystemUtils.OS_NAME} ${SystemUtils.OS_VERSION}")
		LOG.info("Using Java ${Runtime.version()}")
		LOG.info(("Using app data directory $appDataDirectoryPath"))

		CurrentApplicationVersion.codeVersion = codeVersion
		CurrentApplicationVersion.dataVersion = dataVersion

		val settingsEntries = consumeCommandLine(commandLine)
		loadSettings(settingsEntries)
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

	override val defaultUserDataDirectoryPath: Path get() = determineDefaultUserDataDirectoryPath(systemName)

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
				val fileToZip = File(Paths.get(appDataDirectoryPath.toAbsolutePath().toString(), logFileName).toUri())
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
	 *
	 * @return [Settings] entries determined by interpreting the command line and returned explicitly,
	 * because the [Settings] are read by the [DesktopApplication] AFTER this call. In particular,
	 * the [WorkspaceHolder.PROP_WORKSPACE] setting is retrieved by asking the user if the registered
	 * setting is not valid (anymore).
	 */
	protected open fun consumeCommandLine(commandLine: CommandLine): Map<String, Any> {
		consumeDeveloperOption(commandLine)

		val path = determineUserDataDirectoryPath(commandLine)
		return mapOf(WorkspaceHolder.PROP_WORKSPACE to path)
	}

	private fun consumeDeveloperOption(commandLine: CommandLine) {
		EditAuthModule.userHolder = if (commandLine.hasOption(DEVELOPER_OPTION)) {
			LOG.info("Running application in developer mode")
			DesktopUserHolder(DesktopUser.developer)
		} else {
			DesktopUserHolder(DesktopUser.anybody)
		}
	}

	/**
	 * Determines path to the directory where the user data is located.
	 * If not provided on the [CommandLine], it is taken from stored user [Settings], and if that one is not valid,
	 * determined by asking the user for a valid directory.
	 */
	private fun determineUserDataDirectoryPath(commandLine: CommandLine): String {
		val path = if (commandLine.hasOption(USER_DATA_DIR_OPTION)) {
			FileSystems.getDefault().getPath(commandLine.getOptionValue(USER_DATA_DIR_OPTION))
		} else {
			loadSettings(emptyMap())
			if (BaseModule.settings.containsKey(WorkspaceHolder.PROP_WORKSPACE)) {
				Paths.get(BaseModule.settings.get(WorkspaceHolder.PROP_WORKSPACE))
			} else {
				determineAppDataDirectoryPath(commandLine, systemName)
			}
		}

		installWorkspace(path)
		return BaseModule.settings.get(WorkspaceHolder.PROP_WORKSPACE)
	}

	/**
	 * Installs the [Workspace] using the specified [Path]. If that one is not valid, the user is asked
	 * to provide a valid path. If he cancels the dialog, the application quits.
	 */
	private fun installWorkspace(path: Path) {
		try {
			AppModuleJvm.workspaceService.initializeWorkspace(path)
		} catch (e: Exception) {
			if (!WorkspacePanel.showAsDialog(
					title = systemName,
					parent = null,
					application = this,
					userDataDirectoryPath = path.toAbsolutePath().toString(),
					initMode = true,
					introText = Translations.getString("application.openWorkspace.notAvailable.text", systemName),
					initialStatus = e.message ?: "Error")
				) {
                exitProcess(1)
			}
		}
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

	/**
	 * Loads the user [Settings] from persistent storage and sets [cmdLineSettings] which were provided
	 * by the command line.
	 */
	private fun loadSettings(cmdLineSettings: Map<String, Any>) {
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

					for (entry in cmdLineSettings.entries) {
						BaseModule.settings.set(entry.key, entry.value)
					}
				} catch (x: Throwable) {
					LOG.error("Error while loading settings: ${x.message}")
				}
			}
		} catch (_: FileNotFoundException) {
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