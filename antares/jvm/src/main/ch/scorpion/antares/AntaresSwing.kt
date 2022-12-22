package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.antares.view.OrientableRectangularVerticeViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.invocation.ErrorHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.FontIdentification
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.swing.VerticalLabel
import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.library.LibraryIdentification
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
import org.apache.commons.lang3.SystemUtils
import java.awt.*
import java.io.FileInputStream
import java.lang.System
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.JOptionPane
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource


/**
 * The main application class of the Antares digital circuit editor and simulator desktop application,
 * implemented using Swing classes.
 */
class AntaresSwing(
	commandLine: CommandLine,
	private val viewManager: ContentViewManager = DrawViewModule.viewManager
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

		const val ICON_PATH = "img/Logo.png"

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
				FlatDarkLaf.setup()
				UI.isDark = true
			} else {
				FlatLightLaf.setup()
				UI.isDark = false
			}
		}

		private fun establishUiFont(preferences: java.util.Properties) {
			val s = preferences.getProperty(FontIdentification.PROP_FONT_IDENTIFICATION)
			val fontId = s?.let { FontIdentification.parse(it) } ?: FontIdentification()
			val fontResource = if (fontId.isDefault) {
				FontUIResource(Look.UI_FONT.family.fontName, Look.UI_FONT.style, Look.UI_FONT.size)
			} else {
				FontUIResource(fontId.fontName, fontId.style, fontId.size)
			}
			UiUtil.setUIFont(fontResource)
		}

		@JvmStatic
		fun main(args: Array<String>) {

			Thread.setDefaultUncaughtExceptionHandler { _, e ->
				ErrorHandler.exception(e)
			}

			System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS")
			System.setProperty("apple.laf.useScreenMenuBar", "true")
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", AntaresApplication.SYSTEM_NAME)
			System.setProperty("apple.awt.application.name", AntaresApplication.SYSTEM_NAME)

			val commandLine = parseCommandLine(args, defineOptions(Options()), AntaresApplication.SYSTEM_NAME)
			val userDataDirectoryPath = determineUserDataDirectoryPath(commandLine, AntaresApplication.SYSTEM_NAME)

			UIManager.getDefaults()[VerticalLabel.UI_CLASS_ID] = "ch.scorpion.jabbah.base.swing.VerticalLabelUI"
			UIManager.put("Table.intercellSpacing", Dimension(1, 1))
			UIManager.put("TaskPane.contentInsets", Insets(0, 0, 0, 0))
			UIManager.put("TaskPane.roundHeight", 0)

			val preferences = prefetchPreferences(userDataDirectoryPath)
			establishUserLanguage(preferences)
			establishTheme(preferences)
			establishUiFont(preferences)

			BaseModuleJvm.require()

			AntaresSwing(commandLine).start()
		}
	}

	init {
		documentationUrl?.let {
			BaseModule.baseDocumentationUrl = { it }
		}

		if (SystemUtils.IS_OS_MAC) {
			Taskbar.getTaskbar().iconImage = UiUtil.themedIcon("/$ICON_PATH", AntaresSwing::class.java).image
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

	/** ---- [AbstractApplication] */

	override val isFirstUsage: Boolean get() = !ProjectModule.projectManagementService.directoryExists

	override val aboutInfo: AboutInfo get() = AboutInfo(
		iconPath = "/$ICON_PATH",
		name = displayName,
		claim = Translations.getString("antares.claim"),
		version = version)

	/** ---- [AbstractDesktopApplication] */

	override val taskbarIcon: Image get() = Toolkit.getDefaultToolkit().getImage(AntaresSwing::class.java.classLoader.getResource(ICON_PATH))
	//override val taskbarIcon: Image get() = UiUtil.themedIcon(ICON_PATH)

	override fun init() {
		AntaresModuleJvm(this).require()

		super.init()

		AntaresThemes.install()

		systemLibraryBasePath?.let {
			LOG.value.info("Using system libraries in $systemLibraryBasePath")
		}

		customProjectsDirectoryPath?.let {
			LOG.value.info("Using custom projects directory $customProjectsDirectoryPath")
		}

		DrawModuleJvm.contextMenuProvider.applicationName = displayName
	}

	override fun shutdownUI() {
		(mainFrame as AntaresFrameSwing).controller.dispose()
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

	override fun createMenuBarBuilder(): MenuBarBuilder {
		return AntaresMenuBarBuilder(mainFrame as GraphFrameSwing, controller.eventBus)
	}

	override fun createMainFrame(): AbstractApplicationFrame {
		val graphFrameController = AntaresFrameController(controller, controller.eventBus)
		val frame = AntaresFrameSwing(graphFrameController, this, viewManager, graphFrameController)

		frame.graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(OrientableRectangularVerticeViewDrawer())

		return frame
	}

	override fun handleShutdown() {
		super.handleShutdown()

		if (controller.data?.savable is ProjectSavable && (controller.data!!.savable as ProjectSavable).element.library != null) {
			BaseModule.settings.set(PROP_APPLICATION_PROJECT, (controller.data!!.savable as ProjectSavable).project.uuid.toString())
		} else {
			BaseModule.settings.remove(PROP_APPLICATION_PROJECT)
		}
	}

	override fun openInitialSavable() {
		if (commandLine.argList.size > 0) {
			super.openInitialSavable()
			return
		}

		val userId = EditAuthModule.userHolder.user.identity

		val dataViewController = (controller as GraphDataViewController)
		val projectName = BaseModule.settings.getString(PROP_APPLICATION_PROJECT, "")
		if (StringUtils.isNotEmpty(projectName) && ProjectModule.projectManagementService.contains(UUID(projectName))) {
			dataViewController.openProject(LibraryIdentification(UUID(projectName), userId))
			return
		}

		if (isFirstUsage) {
			ProjectModule.projectManagementService
				.createHelloProject(LibraryModule.DEF_LIBRARY_UUID)
				.also { dataViewController.openProject(LibraryIdentification(it.uuid, userId)) }
			return
		}

		dataViewController.closeData()
	}
}