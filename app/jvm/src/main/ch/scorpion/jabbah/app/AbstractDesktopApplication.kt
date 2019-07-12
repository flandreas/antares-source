package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.LOG_SYSTEM
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.preferences.PreferencesChangedEvent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import org.apache.commons.cli.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Files

/** Abstract base implementation of the [DesktopApplication] interface. */
abstract class AbstractDesktopApplication(
	args: Array<String>,
	eventBus: EventBus
) : AbstractApplication(eventBus), DesktopApplication {

	// Must not be in companion object due to Module and LogSystem bootstrapping order
	private val LOG by logger(AbstractDesktopApplication::class)

	protected val commandLine: CommandLine by lazy {
		val options = Options()
		defineOptions(options)
		var cmdLine: CommandLine? = null
		try {
			cmdLine = DefaultParser().parse(options, args)!!
			consumeCommandLine(cmdLine)
		} catch (x: ParseException) {
			LOG.error("Error while parsing options: ${x.message}")
			HelpFormatter().printHelp(displayName, options)
			System.exit(1)
		}
		cmdLine!!
	}

	override val mostRecentSavables: SavableHistory = SavableHistory()

	override var savable: Savable?
		get() = super.savable
		set(value) {
			super.savable = value
			if (value != null && value.supportsMostRecent && value.defined) {
				mostRecentSavables.register(value)
			}
		}

	init {
		loadSettings()
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		super.init()
		loadPreferences()
		LOG_SYSTEM?.level = LogLevel.valueOf(BaseModule.properties.getString(LogSystem.PROP_LOG_LEVEL))
	}

	override fun createNewSavable(): Savable {
		return FileSavable.undefined()
	}

	/** ---- [DesktopApplication] */

	override val homeDirectoryPath: Path
		get() = if (commandLine.hasOption("d")) {
			FileSystems.getDefault().getPath(commandLine.getOptionValue("d"))
		} else {
			FileSystems.getDefault().getPath(System.getProperty("user.home"), systemName)
		}

	override fun quit() {
		if (canReplaceSavable("file.action.quit.name")) {
			shutdown()
		}
	}

	/**
	 * Implements [DesktopApplication.saveTo] by interpreting `identification` as a file system path
	 * and by storing the current [Storable] in a file at that path.
	 */
	override fun saveTo(identification: String) {
		var lFilePath = identification
		if (!lFilePath.endsWith(fileExtension)) {
			lFilePath = "$lFilePath.$fileExtension"
		}
		FileOutputStream(lFilePath).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(applicationData!!)
				savable = FileSavable.withPath(lFilePath)
				eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "application.data.saved.msg"))
			} catch (e: Throwable) {
				LOG.error("Error while saving '$lFilePath': ${e.message}")
			}
		}
	}

	/**
	 * Implements [DesktopApplication.openFrom] by interpreting `identification` as a file system path
	 * and by reading the single [Storable] contained in the file located at that path.
	 * @throws FileNotFoundException if the file at path `identification` doesn't exist
	 */
	override fun openFrom(identification: String): Boolean {
		if (canReplaceSavable("file.action.open.name")) {
			try {
				FileInputStream(identification).use {
					return try {
						val storeReader = StoreXmlReader(ElectricXmlReader(it))
						val drawing = storeReader.readStorable()
						applicationData = drawing
						savable = FileSavable.withPath(identification)
						true
					} catch (e: Throwable) {
						LOG.error("Error while opening '$identification': ${e.cause}")
						false
					}
				}
			} catch (e: FileNotFoundException) {
				throw IllegalArgumentException()
			}
		}
		return false
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
		System.exit(0)
	}

	/**
	 * Defines the command line argument [Options] for this [DesktopApplication].
	 * This implementation defines an option for the 'home directory'. Subclasses can overwrite this method
	 * to add additional options, or to replace it with others.
	 */
	protected open fun defineOptions(options: Options) {
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
	}

	/**
	 * Called by this [AbstractDesktopApplication] after the options have been parsed.
	 * This implementation does nothing. Subclasses can overwrite this method in order to consume and use
	 * the provided [Options].
	 */
	protected open fun consumeCommandLine(commandLine: CommandLine) {
		AppModule.userHolder.u = if (commandLine.hasOption("dev")) {
			LOG.info("Starting application in developer mode")
			User.developer()
		} else {
			User.anybody()
		}
	}

	private fun getSettingsPath(): Path {
		return FileSystems.getDefault().getPath(homeDirectoryPath.toString(), "$systemName.ini")
	}

	private fun getPreferencesPath(): Path {
		return FileSystems.getDefault().getPath(homeDirectoryPath.toString(), "$systemName.pref")
	}

	/** Ensures that the application home directory exists by creating it if it doesn't. */
	private fun ensureHomeDirectory() {
		val path = homeDirectoryPath
		if (Files.notExists(path)) {
			LOG.debug("Creating home directory '$path'")
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

	protected fun storeSettings() {
		val path = getSettingsPath()
		LOG.debug("Storing settings in $path")
		ensureHomeDirectory()
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
					eventBus.post(PreferencesChangedEvent(BaseModule.properties))
				} catch (x: Throwable) {
					LOG.error("Error while loading preferences: ${x.message}")
				}
			}
		} catch (x: FileNotFoundException) {
			// empty
		}
	}

	protected fun storePreferences() {
		val path = getPreferencesPath()
		LOG.debug("Storing preferences in $path")
		ensureHomeDirectory()
		FileOutputStream(path.toString()).use {
			try {
				val preferences = java.util.Properties()
				for (key in BaseModule.properties.getCustomizedKeys()) {
					preferences.set(key, BaseModule.properties.getEntry(key).stringValue)
				}
				preferences.store(it, null)
			} catch (x: Throwable) {
				LOG.error("Error while storing preferences: ${x.message}")
			}
		}
	}
}