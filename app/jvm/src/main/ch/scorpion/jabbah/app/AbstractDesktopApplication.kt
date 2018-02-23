package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
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

	override fun saveFile(filePath: String) {
		var lFilePath = filePath
		if (!lFilePath.endsWith(fileExtension)) {
			lFilePath = lFilePath + "." + fileExtension
		}
		FileOutputStream(filePath).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(applicationData!!)
				savable = FileSavable.withPath(lFilePath)
			} catch (e: Throwable) {
				LOG.error("Error while saving '$lFilePath': ${e.message}")
			}
		}
	}

	override fun openFile(filePath: String): Boolean {
		if (canReplaceSavable("file.action.open.name")) {
			FileInputStream(filePath).use {
				try {
					val storeReader = StoreXmlReader(ElectricXmlReader(it))
					val drawing = storeReader.readStorable()
					applicationData = drawing
					savable = FileSavable.withPath(filePath)
					return true
				} catch (e: Throwable) {
					LOG.error("Error while opening '$filePath': ${e.cause}")
					return false
				}
			}
		}
		return false
	}

	/** ---- [AbstractDesktopApplication] */

	protected abstract fun shutdownUI()

	protected fun shutdown() {
		LOG.info("Shutting $displayName down")
		// TODO Provide service for loading/storing Properties
		shutdownUI()
		storeProperties()
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
	}

	/**
	 * Called by this [AbstractDesktopApplication] after the options have been parsed.
	 * This implementation does nothing. Subclasses can overwrite this method in order to consume and use
	 * the provided [Options].
	 */
	protected open fun consumeCommandLine(@Suppress("UNUSED_PARAMETER") commandLine: CommandLine) {
		// empty
	}

	private fun getSettingsPath(): Path {
		return FileSystems.getDefault().getPath(homeDirectoryPath.toString(), systemName + ".ini")
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
					LOG.error("Could not load properties: ${x.message}")
				}
			}
		} catch (x: FileNotFoundException) {
			// empty
		}
	}

	private fun storeProperties() {
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
				LOG.error("Error while storing properties: ${x.message}")
			}
		}
	}
}