package ch.scorpion.antares

import ch.scorpion.antares.AntaresApplication.Companion.DEF_LIBRARY_UUID
import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.invocation.ErrorHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.ProjectSavable
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.GraphFrameSwing
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.SystemUtils
import java.awt.Image
import java.awt.Taskbar
import java.awt.Toolkit
import java.io.FileInputStream
import java.lang.System
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.JOptionPane
import javax.swing.plaf.FontUIResource


/**
 * The main application class of the Antares digital circuit editor and simulator desktop application,
 * implemented using Swing classes.
 */
class AntaresSwing(
	commandLine: CommandLine,
	private val viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractDesktopApplicationSwing(
	commandLine,
	GraphDataViewController()
), AntaresDesktop {

	companion object {

		private val LOG by lazy { logger(AntaresSwing::class) }

		private const val PROP_APPLICATION_PROJECT = "application.project"

		private const val SYSTEM_LIB_BASE_OPTION = "sl"
		private const val FILE_STORE_BASE_OPTION = "fs"
		private const val PROJECT_DIR_OPTION = "p"
		private const val USER_LIBRARY_DIR_OPTION = "l"
		private const val URL_OPTION = "url"

		private fun defineOptions(options: Options): Options {
			AbstractDesktopApplication.defineOptions(options)

			options.addOption(Option.builder(SYSTEM_LIB_BASE_OPTION)
				.required(false)
				.longOpt("syslibBase")
				.desc("System library base location")
				.hasArg()
				.build())

			options.addOption(Option.builder(FILE_STORE_BASE_OPTION)
				.required(false)
				.longOpt("fileStore")
				.desc("File store base location")
				.hasArg()
				.build())

			options.addOption(Option.builder(PROJECT_DIR_OPTION)
				.required(false)
				.longOpt("projects")
				.desc("Project directory name")
				.hasArg()
				.build())

			options.addOption(Option.builder(USER_LIBRARY_DIR_OPTION)
				.required(false)
				.longOpt("userLibraries")
				.desc("User library directory name")
				.hasArg()
				.build())

			options.addOption(Option.builder(URL_OPTION)
				.required(false)
				.longOpt("url")
				.desc("Server URL")
				.hasArg()
				.build())

			return options
		}

		private fun prefetchPreferences(userDataDirectoryPath: Path): java.util.Properties {
			val filePath = Paths.get(userDataDirectoryPath.toString(), "${AntaresApplication.SYSTEM_NAME}.$PREFERENCES_FILE_EXTENSION")
			val settings = java.util.Properties()
			if (Files.exists(filePath)) {
				FileInputStream(filePath.toString()).use {
					settings.load(it)
				}
			}
			return settings
		}

		/**
		 * Read the persistent user language preference and establish it as the default Locale
		 * as early as possible in the boot-strap phase. This method does not provide the
		 * preference [Properties] for subsequent usage by the application. This is the duty
		 * of [AbstractDesktopApplication.loadPreferences].
		 */
		private fun establishUserLanguage(preferences: java.util.Properties) {
			val lang = preferences.getProperty(Language.PROP_LANGUAGE) ?: Language.DEFAULT.code
			val locale = Locale(lang)
			Locale.setDefault(locale)
			JOptionPane.setDefaultLocale(locale)
		}

		/**
		 * Read the dark property before the first UI element is created, which is unfortunately
		 * already in the Module boot-strap sequence.
		 */
		private fun establishTheme(preferences: java.util.Properties) {
			val name = preferences.getProperty(Themes.PROP_THEME)

			// TODO: Okay, this is a terrible hack. Currently don't know how to access the current Theme
			// before the Themes have been registered later in the boot-strap sequence.
			if (name == "CRT" || name == "Darcula") {
				FlatDarkLaf.install()
				UI.isDark = true
			} else {
				FlatLightLaf.install()
				UI.isDark = false
			}
		}

		@JvmStatic
		fun main(args: Array<String>) {

			Thread.setDefaultUncaughtExceptionHandler { _, e ->
				ErrorHandler.exception(e)
			}

			System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS")
			System.setProperty("apple.laf.useScreenMenuBar", "true")
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", AntaresApplication.SYSTEM_NAME)

			val commandLine = parseCommandLine(args, defineOptions(Options()), AntaresApplication.SYSTEM_NAME)
			val userDataDirectoryPath = determineUserDataDirectoryPath(commandLine, AntaresApplication.SYSTEM_NAME)

			val preferences = prefetchPreferences(userDataDirectoryPath)
			establishUserLanguage(preferences)
			establishTheme(preferences)

			UiUtil.setUIFont(FontUIResource(Look.UI_FONT.family.javaName, Look.UI_FONT.style, Look.UI_FONT.size))

			BaseModuleJvm.require()
			AntaresSwing(commandLine).start()
		}
	}
	private val iconPath = "img/Logo64.png"

	init {
		if (SystemUtils.IS_OS_MAC) {
			Taskbar.getTaskbar().iconImage = Toolkit.getDefaultToolkit().getImage(AntaresSwing::class.java.classLoader.getResource("img/Logo64.png"))
		}
	}

	/** ---- [AntaresDesktop] */

	private var customFileStoreBasePath: String? = null

	private var customProjectsDirectoryPath: String? = null

	private var customProjectDirectoryName: String? = null

	private var customUserLibraryDirectoryName: String? = null

	override var systemLibraryBasePath: String? = null
		private set

	override val fileStoreBasePath: String get() = customFileStoreBasePath ?: super.fileStoreBasePath

	override val projectDirectoryName: String get() = customProjectDirectoryName ?: super.projectDirectoryName

	override val userLibraryDirectoryName: String get() = customUserLibraryDirectoryName ?: super.userLibraryDirectoryName

	override val dataLocation: DataLocation get() = DataLocation.withName(BaseModule.properties.getString(DataLocation.PROP_DATA_LOCATION))

	override var dataUrl: URL? = null
		private set

	/** ---- [AbstractApplication] */

	override val aboutInfo: AboutInfo get() = AboutInfo(
		iconPath = "/$iconPath",
		name = displayName,
		claim = Translations.getString("antares.claim"),
		version = readVersion())

	override fun readVersion(): ApplicationVersion = ApplicationVersion.parse(
		IOUtils.toString(this.javaClass.getResourceAsStream("/version.txt"), "UTF-8"))

	/** ---- [AbstractDesktopApplication] */

	override val taskbarIcon: Image get() = Toolkit.getDefaultToolkit().getImage(AntaresSwing::class.java.classLoader.getResource(iconPath))

	override fun init() {
		AntaresModuleJvm(this).require()
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(DEF_LIBRARY_UUID, isSystem = true)

		super.init()

		AntaresThemes.install()

		systemLibraryBasePath?.let {
			LOG.value.info("Using system libraries in $systemLibraryBasePath")
		}

		customProjectsDirectoryPath?.let {
			LOG.value.info("Using custom projects directory $customProjectsDirectoryPath")
		}
	}

	override fun consumeCommandLine(commandLine: CommandLine) {
		super.consumeCommandLine(commandLine)
		if (commandLine.hasOption(SYSTEM_LIB_BASE_OPTION)) {
			consumeSystemLibraryBasePath(commandLine.getOptionValue(SYSTEM_LIB_BASE_OPTION))
		}
		if (commandLine.hasOption(FILE_STORE_BASE_OPTION)) {
			consumeFileStoreBasePath(commandLine.getOptionValue(FILE_STORE_BASE_OPTION))
		}
		if (commandLine.hasOption(PROJECT_DIR_OPTION)) {
			consumeProjectDirectoryName(commandLine.getOptionValue(PROJECT_DIR_OPTION))
		}
		if (commandLine.hasOption(USER_LIBRARY_DIR_OPTION)) {
			consumeUserLibraryDirectoryName(USER_LIBRARY_DIR_OPTION)
		}
		if (commandLine.hasOption(URL_OPTION)) {
			consumeUrl(commandLine.getOptionValue(URL_OPTION))
		}
	}

	private fun consumeSystemLibraryBasePath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("System library base directory $path not found")
			return
		}
		systemLibraryBasePath = path
	}

	private fun consumeFileStoreBasePath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("FIle store base directory $path not found")
			return
		}
		customFileStoreBasePath = path
	}

	private fun consumeCustomProjectsDirectoryPath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("Projects directory $path not found")
			return
		}
		customProjectsDirectoryPath = path
	}

	private fun consumeProjectDirectoryName(name: String) {
		customProjectDirectoryName = name
	}

	private fun consumeUserLibraryDirectoryName(name: String) {
		customUserLibraryDirectoryName = name
	}

	private fun consumeUrl(url: String) {
		try {
			dataUrl = URL(url)
		} catch (e: MalformedURLException) {
			println("Invalid URL $url")
		}
	}

	override fun createMenuBarBuilder(): MenuBarBuilder {
		return AntaresMenuBarBuilder(mainFrame as GraphFrameSwing, controller.eventBus)
	}

	override fun createMainFrame(): AbstractApplicationFrame {
		val graphFrameController = AntaresFrameController(controller, controller.eventBus)
		val frame = AntaresFrameSwing(graphFrameController, this, controller.eventBus, viewManager, graphFrameController)

		frame.graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(DigitalComponentViewDrawer())

		return frame
	}

	override fun handleShutdown() {
		super.handleShutdown()
		if (controller.data?.savable is ProjectSavable) {
			BaseModule.settings.set(PROP_APPLICATION_PROJECT, (controller.data!!.savable as ProjectSavable).project.uuid.toString())
		} else if (controller.data?.savable != null) {
			BaseModule.settings.remove(PROP_APPLICATION_PROJECT)
		}
	}

	override fun openInitialSavable() {
		if (commandLine.argList.size > 0) {
			super.openInitialSavable()
			return
		}

		val dataViewController = (controller as GraphDataViewController)
		val projectName = BaseModule.settings.getString(PROP_APPLICATION_PROJECT, "")
		if (StringUtils.isNotEmpty(projectName) && ProjectModule.projectManagementService.contains(UUID(projectName))) {
			dataViewController.openProject(UUID(projectName))
			return
		}

		if (!ProjectModule.projectManagementService.directoryExists) {
			ProjectModule.projectManagementService
				.createHelloProject(DEF_LIBRARY_UUID)
				.also { dataViewController.openProject(it.uuid) }
			return
		}

		dataViewController.closeData()
	}
}