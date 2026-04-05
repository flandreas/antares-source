package io.antarescircuit.antares

import io.antarescircuit.antares.ui.AntaresContextMenuProvider
import io.antarescircuit.antares.view.AntaresFrameController
import io.antarescircuit.antares.view.DigitalComponentViewDrawer
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.theme.AntaresThemes
import io.antarescircuit.jabbah.app.*
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.invocation.ErrorHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.preferences.FontIdentification
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.swing.VerticalLabel
import io.antarescircuit.jabbah.base.swing.VerticalLabelUI
import io.antarescircuit.jabbah.base.swing.taskpane.JabbahTaskPaneContainer
import io.antarescircuit.jabbah.base.swing.taskpane.JabbahTaskPaneContainerUI
import io.antarescircuit.jabbah.base.ui.UI
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.LibraryModule.libraryHolder
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.project.ProjectSavable
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.ui.GraphFrameSwing
import io.antarescircuit.jabbah.io.ElectricXmlReader
import io.antarescircuit.jabbah.io.StoreXmlReader
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import kotlinx.coroutines.runBlocking
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
import kotlin.system.exitProcess


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

		/** The property in [Settings] storing the UUID of the most recently open project.*/
		private const val PROP_APPLICATION_PROJECT = "application.project"

		/** The property in [Settings] storing the UUID of the most recently open library.*/
		private const val PROP_APPLICATION_LIBRARY = "application.library"

		/** The property in [Settings] storing the UUID of the [MetaGraph] in the most recently open project or library.*/
		private const val PROP_META_GRAPH = "application.metaGraph"

		private const val SYSTEM_LIB_BASE_OPTION = "sl"
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

		private fun prefetchPreferences(appDataDirectoryPath: Path): java.util.Properties {
			val filePath = Paths.get(appDataDirectoryPath.toString(), "${AntaresApplication.SYSTEM_NAME}.$PREFERENCES_FILE_EXTENSION")
			val properties = java.util.Properties()
			if (Files.exists(filePath)) {
				FileInputStream(filePath.toString()).use {
					properties.load(it)
				}
			}
			return properties
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
			LOG.value.userTrail("Using language '$lang'")
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

		private suspend fun importLibrary(path: String) {
			LOG.value.info("Importing $path")
			val process = AbstractLibraryImportProcess.forPath(path) { library, process ->
				process.open(library)
			}

			if (process == null) {
				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					Translations.getString("antares.importOnStartup.unknownExtension.msg"),
					Translations.getString("antares.importOnStartup.title"),
					JOptionPane.ERROR_MESSAGE
				)
			} else {
				process.import(path)
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
			System.setProperty("apple.awt.application.name", AntaresApplication.SYSTEM_NAME)

			val commandLine = parseCommandLine(args, defineOptions(Options()), AntaresApplication.SYSTEM_NAME)
			val appDataDirectoryPath = determineAppDataDirectoryPath(commandLine, AntaresApplication.SYSTEM_NAME)

			UIManager.getDefaults()[VerticalLabel.UI_CLASS_ID] = VerticalLabelUI::class.qualifiedName
			UIManager.getDefaults()[JabbahTaskPaneContainer.UI_CLASS_ID] = JabbahTaskPaneContainerUI::class.qualifiedName
			UIManager.put("Table.intercellSpacing", Dimension(1, 1))
			UIManager.put("TaskPane.contentInsets", Insets(0, 0, 0, 0))
			UIManager.put("TaskPane.roundHeight", 0)

			val preferences = prefetchPreferences(appDataDirectoryPath)
			establishUserLanguage(preferences)
			establishTheme(preferences)
			establishUiFont(preferences)

			// BaseModuleJmv is not enough. Also need app texts for WorkspacePanel.
			AppModuleJvm.require()

			AntaresSwing(commandLine).start()
		}
	}

	init {

		// Cannot put this in Properties object, would be initialized too late for access by graph module
		AppModuleJvm.remotePropertiesUrl = "https://www.antarescircuit.io/remote.properties"

		documentationUrl?.let { BaseModule.baseDocumentationUrl = { it } }

		if (SystemUtils.IS_OS_MAC) {
			Taskbar.getTaskbar().iconImage = UiUtil.themedIcon("/$ICON_PATH", AntaresSwing::class.java).image
		}
	}

	/** ---- [AntaresDesktop] */

	private var customFileStoreBasePath: String? = null

	private var customProjectDirectoryName: String? = null

	private var customUserLibraryDirectoryName: String? = null

	override var systemLibraryBasePath: String? = null
		private set

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

	override fun init() {
		AntaresModuleJvm(this).require()

		super.init()

		AntaresThemes.install()

		systemLibraryBasePath?.let {
			LOG.value.info("Using system libraries in $systemLibraryBasePath")
		}

		DrawModuleJvm.contextMenuProvider.applicationName = displayName
	}

	override fun shutdownUI() {
		(mainFrame as AntaresFrameSwing).controller.dispose()
	}

	override fun consumeCommandLine(commandLine: CommandLine): Map<String, Any> {
		val settingsEntries = super.consumeCommandLine(commandLine)
		if (commandLine.hasOption(SYSTEM_LIB_BASE_OPTION)) {
			consumeSystemLibraryBasePath(commandLine.getOptionValue(SYSTEM_LIB_BASE_OPTION))
		}
		if (commandLine.hasOption(PROJECT_DIR_OPTION)) {
			consumeProjectDirectoryName(commandLine.getOptionValue(PROJECT_DIR_OPTION))
		}
		if (commandLine.hasOption(USER_LIBRARY_DIR_OPTION)) {
			consumeUserLibraryDirectoryName(USER_LIBRARY_DIR_OPTION)
		}
		return settingsEntries
	}

	private fun consumeSystemLibraryBasePath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("System library base directory $path not found")
			return
		}
		systemLibraryBasePath = path
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

		frame.graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(DigitalComponentViewDrawer())
		DrawModuleJvm.contextMenuProvider = AntaresContextMenuProvider(this)

		return frame
	}

	override fun handleShutdown() {
		super.handleShutdown()

		BaseModule.settings.remove(PROP_APPLICATION_LIBRARY)
		BaseModule.settings.remove(PROP_APPLICATION_PROJECT)
		BaseModule.settings.remove(PROP_META_GRAPH)

		if (libraryHolder.l is Project) {
			BaseModule.settings.set(PROP_APPLICATION_PROJECT, (libraryHolder.l as Project).uuid.id)
			if (controller.data?.savable is ProjectSavable) {
				BaseModule.settings.set(PROP_META_GRAPH, (controller.data!!.savable as ProjectSavable).element.uuid.id)
			}
		} else if (libraryHolder.l is Library) {
			BaseModule.settings.set(PROP_APPLICATION_LIBRARY, (libraryHolder.l as Library).uuid.id)
			if (controller.data?.savable is LibrarySavable) {
				BaseModule.settings.set(PROP_META_GRAPH, (controller.data!!.savable as LibrarySavable).element.uuid.id)
			}
		}
	}

	override fun openInitialSavable() {
		if (commandLine.argList.size > 0) {
			handleCommandLineArgument(commandLine.argList[0])
			return
		}

		if (launchDataPath != null) {
			handleCommandLineArgument(launchDataPath!!)
			return
		}

		val userId = EditAuthModule.userHolder.user.identity

		val dataViewController = (controller as GraphDataViewController)

		val metaGraphUuid = BaseModule.settings.getString(PROP_META_GRAPH, "")

		val projectUuid = BaseModule.settings.getString(PROP_APPLICATION_PROJECT, "")
		try {
			if (StringUtils.isNotEmpty(projectUuid) && ProjectModule.projectManagementService.contains(UUID(projectUuid))) {
				val projectId = LibraryIdentification(UUID(projectUuid), userId)
				if (StringUtils.isNotEmpty(metaGraphUuid)) {
					dataViewController.openProject(projectId, UUID(metaGraphUuid))
				} else {
					dataViewController.openProject(projectId)
				}
				return
			}
		} catch (x: Exception) {
			LOG.value.error("Error opening initial savable $projectUuid", x)
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				Translations.getString("antares.openInitialSavable.error.msg", x.message ?: ""),
				Translations.getString("base.error.txt"),
				JOptionPane.ERROR_MESSAGE
			)
		}

		val libraryUuid = BaseModule.settings.getString(PROP_APPLICATION_LIBRARY, "")
		try {
			if (StringUtils.isNotEmpty(libraryUuid) && LibraryModule.libraryManagementService.contains(UUID(libraryUuid))) {
				val libraryId = LibraryIdentification(UUID(libraryUuid), userId)
				if (StringUtils.isNotEmpty(metaGraphUuid)) {
					dataViewController.openLibrary(libraryId, UUID(metaGraphUuid))
				} else {
					dataViewController.openLibrary(libraryId)
				}
				return
			}
		} catch (x: Exception) {
			LOG.value.error("Error opening initial library $projectUuid", x)
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				Translations.getString("antares.openInitialSavable.error.msg", x.message ?: ""),
				Translations.getString("base.error.txt"),
				JOptionPane.ERROR_MESSAGE
			)
		}

		if (isFirstUsage) {
			try {
				createHelloProject(EditAuthModule.userHolder.user.identity)
			} catch (e: Exception) {
				LOG.value.error("Error in creating data dir / hello project. Shutting down...", e)
				val msg = Translations.getString("antares.createDataDir.error.msg", e.message ?: "", getDefaultAppDataDirectory())
				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					msg,
					Translations.getString("antares.createDataDir.title"),
					JOptionPane.ERROR_MESSAGE
				)
				exitProcess(-1)
			}
			return
		}

		dataViewController.closeData()
	}

	private fun createHelloProject(userId: UserIdentity) {
		ProjectModule.projectManagementService
			.createHelloProject(LibraryModule.DEF_LIBRARY_UUID, loadHelloCircuit())
			.also {
				(controller as GraphDataViewController).openProject(LibraryIdentification(it.uuid, userId))
				(mainFrame as AntaresFrameSwing).controller.graphPanelViewController.libraryPanelController
					.libraryTreeViewController.view.expandFolder(AntaresApplication.FREQUENTLY_USED_FOLDER_NAME_EN)
			}
	}

	private fun loadHelloCircuit(): MetaGraph {
		try {
			AntaresSwing::class.java.getResourceAsStream("/my-first-circuit.cir").use { input ->
				return StoreXmlReader(ElectricXmlReader(input!!)).readStorable()
			}
		} catch (e: Exception) {
			LOG.value.error("Error while loading my-first-circuit.cir", e)
			throw e
		}
	}

	private fun handleCommandLineArgument(path: String) {
		if (JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.importOnStartup.question", path),
			Translations.getString("antares.importOnStartup.title"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE
		) == JOptionPane.YES_OPTION) {
			runBlocking {
				importLibrary(path)
			}
		}
	}

	override fun openFile(path: String) {
		handleCommandLineArgument(path)
	}
}