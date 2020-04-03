package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.DefaultLightColorEvent
import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.app.DigitalGraphViewService
import ch.scorpion.antares.view.memory.MemoryContentGraphDesktopItem
import ch.scorpion.antares.view.memory.MemoryContentsPanel
import ch.scorpion.antares.view.memory.OpenMemoryContentsRequest
import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.ui.GraphFrameSwing
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.Storable
import com.formdev.flatlaf.FlatLightLaf
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.io.IOUtils
import java.awt.*
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.plaf.FontUIResource


/**
 * The main application class of the Antares digital circuit editor and simulator desktop application,
 * implemented using Swing classes.
 */
class AntaresSwing(
	commandLine: CommandLine,
	eventBus: EventBus = BaseModule.eventBus,
	private val viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractDesktopApplicationSwing(commandLine, eventBus), Antares {


	companion object {

		private val LOG by lazy { logger(AntaresSwing::class) }

		private const val PROP_APPLICATION_PROJECT = "application.project"
		private val DEF_LIBRARY_UUID = UUID("6707f981-110d-4629-a0bf-c35a4688025c")

		fun defineOptions(options: Options): Options {
			AbstractDesktopApplication.defineOptions(options)

			options.addOption(Option.builder("t")
				.required(false)
				.longOpt("theme")
				.desc("Theme")
				.hasArg()
				.build())

			options.addOption(Option.builder("l")
				.required(false)
				.longOpt("syslib")
				.desc("System library location")
				.hasArg()
				.build())

			return options
		}


		@JvmStatic
		fun main(args: Array<String>) {

			Thread.setDefaultUncaughtExceptionHandler { _, e ->
				println("Unhandled exception: ${e.message}")
				e.printStackTrace()
				LOG.value.error("Unhandled exception", e)
			}

			System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS")
			System.setProperty("apple.laf.useScreenMenuBar", "true")
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", Antares.SYSTEM_NAME)

			FlatLightLaf.install()
			UiUtil.setUIFont(FontUIResource(Look.UI_FONT.family.javaName, Look.UI_FONT.style, Look.UI_FONT.size))

			val commandLine = parseCommandLine(args, defineOptions(Options()), Antares.SYSTEM_NAME)
			determineUserDataDirectoryPath(commandLine, Antares.SYSTEM_NAME)

			BaseModuleJvm.require()
			AntaresSwing(commandLine).start()
		}
	}
	private val iconPath = "img/Logo64.png"

	init {

		Taskbar.getTaskbar().iconImage = Toolkit.getDefaultToolkit().getImage(AntaresSwing::class.java.classLoader.getResource("img/Logo64.png"))

		eventBus.register(OpenMemoryContentsRequest::class) { request ->
			if (request.newDesktopView) {
				(mainFrame as GraphFrameSwing).graphPanel.desktopController.openVerticeView(request.verticeView) {
					MemoryContentGraphDesktopItem(
						memory = request.memory,
						addressable = request.addressable,
						title = request.name,
						cmdManager = mainFrame.editor.commandManager,
						readonly = request.readonly,
						contextColor = it)
				}
			} else {
				MemoryContentsPanel.showAsDialog(
					parent = mainFrame,
					name = request.name,
					memory = request.memory,
					addressable = request.addressable,
					cmdManager = mainFrame.editor.commandManager,
					readonly = request.readonly)
			}
		}
		eventBus.register(OpenProjectRequest::class) {
			if (!canReplaceSavable("project.action.open.name")) {
				throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
			}
		}
		eventBus.register(OpenLibraryRequest::class) {
			if (!canReplaceSavable("library.action.open.name")) {
				throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
			}
		}
		eventBus.register(CloseProjectRequest::class) {
			if (savable is ProjectSavable && (savable as ProjectSavable).project == it.project && !canReplaceSavable("project.action.close.name")) {
				throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
			}
		}
		eventBus.register(CurrentLibraryEvent::class) {
			close()
		}

		eventBus.register(CurrentProjectEvent::class) {
			close()
		}

		eventBus.register(LibraryItemRemovedEvent::class) {
			if (it.item is ContainerLibraryElement && (it.item as ContainerLibraryElement).metaGraph == applicationData) {
				SwingUtilities.invokeLater { close() }
			}
		}

		eventBus.register(DefaultLightColorEvent::class) {
			if (it.graphView.defaultLightColor != null && shouldReplaceLightColor()) {
				(GraphViewModule.graphViewService as DigitalGraphViewService).replaceLightColor(it.graphView)
			}
		}
	}

	/** ---- [Antares] */

	override var systemLibraryDirectoryPath: String? = null
		private set

	/** ---- [AbstractApplication] */

	override val aboutInfo: AboutInfo
		get() = AboutInfo(
			iconPath = "/$iconPath",
			name = displayName,
			claim = Translations.getString("antares.claim"),
			version = readVersion())

	private fun readVersion(): String = IOUtils.toString(this.javaClass.getResourceAsStream("/version.txt"), "UTF-8")

	/** ---- [AbstractDesktopApplication] */

	override val taskbarIcon: Image get() = Toolkit.getDefaultToolkit().getImage(AntaresSwing::class.java.classLoader.getResource(iconPath))

	override fun createNewApplicationData(): Storable {
		return MetaGraph()
	}

	override fun init() {
		AntaresModuleJvm(this).require()
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(DEF_LIBRARY_UUID, isSystem = true)
		AntaresThemes.install()
		super.init()

		systemLibraryDirectoryPath?.let {
			LOG.value.info("Using system libraries in $systemLibraryDirectoryPath")
		}
	}

	override fun consumeCommandLine(commandLine: CommandLine) {
		super.consumeCommandLine(commandLine)
		if (commandLine.hasOption("t")) {
			Themes.setCurrent(commandLine.getOptionValue("t"))
		}
		if (commandLine.hasOption("l")) {
			consumeSystemLibraryDirectoryPath(commandLine.getOptionValue("l"))
		}
	}

	private fun consumeSystemLibraryDirectoryPath(path: String) {
		if (Files.notExists(Paths.get(path))) {
			println("System library directory $path not found")
			return
		}

		systemLibraryDirectoryPath = path
	}

	override fun createMenuBarBuilder(): MenuBarBuilder {
		return AntaresMenuBarBuilder(mainFrame as GraphFrameSwing, eventBus)
	}

	override fun createMainFrame(): AbstractApplicationFrame {
		val controller = GraphFrameController(eventBus)
		val frame = GraphFrameSwing(this, eventBus, viewManager, controller)
		controller.view = frame

		frame.graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(DigitalComponentViewDrawer())

		return frame
	}

	/** Implements [DesktopApplication.openFrom] by interpreting `identification` as a project [UUID].*/
	override fun openFrom(identification: String): Boolean {
		InvocationHandler.invoke(Runnable {
			ProjectModule.projectManagementService.open(UUID(identification))
		})
		return true
	}

	override fun handleShutdown() {
		super.handleShutdown()
		if (savable is ProjectSavable) {
			BaseModule.settings.set(PROP_APPLICATION_PROJECT, (savable as ProjectSavable).project.uuid.toString())
		} else if (savable != null) {
			BaseModule.settings.remove(PROP_APPLICATION_PROJECT)
		}
	}

	override fun openInitialSavable() {
		if (commandLine.argList.size > 0) {
			super.openInitialSavable()
			return
		}

		val projectName = BaseModule.settings.getString(PROP_APPLICATION_PROJECT, "")
		if (StringUtils.isNotEmpty(projectName)) {
			openFrom(projectName)
			return
		}

		if (!ProjectModule.projectManagementService.directoryExists) {
			ProjectModule.projectManagementService
				.createHelloProject(DEF_LIBRARY_UUID)
				.also { openFrom(it.uuid.toString()) }
			return
		}

		close()
	}

	/** ---- [AntaresSwing] */

	private fun shouldReplaceLightColor(): Boolean {
		return JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.action.replaceLightColor.question"),
			Translations.getString("antares.action.replaceLightColor.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
	}
}