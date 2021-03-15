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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
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

		private const val SYSTEM_LIB_OPTION = "l"
		private const val PROJECTS_OPTION = "p"

		private fun defineOptions(options: Options): Options {
			AbstractDesktopApplication.defineOptions(options)

			options.addOption(Option.builder(SYSTEM_LIB_OPTION)
				.required(false)
				.longOpt("syslib")
				.desc("System library location")
				.hasArg()
				.build())

			options.addOption(Option.builder(PROJECTS_OPTION)
				.required(false)
				.longOpt("projects")
				.desc("Projects location")
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
			preferences.getProperty(Language.PROP_LANGUAGE)?.let { lang -> Locale.setDefault(Locale(lang)) }
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

	private var customProjectsDirectoryPath: String? = null

	override var systemLibraryDirectoryPath: String? = null
		private set

	override val projectsDirectoryPath: String get() = customProjectsDirectoryPath ?: super.projectsDirectoryPath

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

		systemLibraryDirectoryPath?.let {
			LOG.value.info("Using system libraries in $systemLibraryDirectoryPath")
		}

		customProjectsDirectoryPath?.let {
			LOG.value.info("Using custom projects directory $customProjectsDirectoryPath")
		}
	}

	override fun consumeCommandLine(commandLine: CommandLine) {
		super.consumeCommandLine(commandLine)
		if (commandLine.hasOption(SYSTEM_LIB_OPTION)) {
			consumeSystemLibraryDirectoryPath(commandLine.getOptionValue(SYSTEM_LIB_OPTION))
		}
		if (commandLine.hasOption(PROJECTS_OPTION)) {
			consumeCustomProjectsDirectoryPath(commandLine.getOptionValue(PROJECTS_OPTION))
		}
	}

	private fun consumeSystemLibraryDirectoryPath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("System library directory $path not found")
			return
		}
		systemLibraryDirectoryPath = path
	}

	private fun consumeCustomProjectsDirectoryPath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("Projects directory $path not found")
			return
		}
		customProjectsDirectoryPath = path
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