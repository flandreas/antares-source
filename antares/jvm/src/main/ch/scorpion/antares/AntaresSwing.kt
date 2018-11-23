package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.memory.MemoryContentsPanel
import ch.scorpion.antares.view.memory.OpenMemoryContentsRequest
import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphPanel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.io.Storable
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import javax.swing.JDialog
import javax.swing.plaf.FontUIResource


/**
 * The main application class of the Antares digital circuit editor and simulator desktop application,
 * implemented using Swing classes. Will be replaced by [AntaresFx].
 */
class AntaresSwing(
	args: Array<String>,
	eventBus: EventBus = BaseModule.eventBus,
	private val viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractDesktopApplicationSwing(args, eventBus), Antares {

	companion object {

		private const val PROP_APPLICATION_PROJECT = "application.project"

		@JvmStatic
		fun main(args: Array<String>) {
			BaseModuleJvm.require()
			AntaresSwing(args).start()
		}
	}

	init {
		eventBus.register(OpenMemoryContentsRequest::class) { handle(it) }
		eventBus.register(OpenProjectRequest::class) {
			if (!canReplaceSavable("project.action.open.name")) {
				throw VetoException()
			}
		}
		eventBus.register(OpenLibraryRequest::class) {
			if (!canReplaceSavable("library.action.open.name")) {
				throw VetoException()
			}
		}
		eventBus.register(CloseProjectRequest::class) {
			if (savable is ProjectSavable && (savable as ProjectSavable).project == it.project && !canReplaceSavable("project.action.close.name")) {
				throw VetoException()
			}
		}
		eventBus.register(CurrentLibraryEvent::class) {
			close()
		}
		eventBus.register(LibraryItemRemovedEvent::class) {
			if (it.item is ContainerLibraryElement && (it.item as ContainerLibraryElement).metaGraph == applicationData) {
				close()
			}
		}
	}

	/** ---- [AbstractDesktopApplication] */

	override fun createNewApplicationData(): Storable {
		return MetaGraph()
	}

	override fun init() {
		AntaresModuleJvm(this).require()
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.invoke().loadLibrary("Standard")
		UiUtil.setUIFont(FontUIResource(Look.UI_FONT.family.javaName, Look.UI_FONT.style, Look.UI_FONT.size))
		AntaresThemes.install()
		super.init()
	}

	override fun defineOptions(options: Options) {
		super.defineOptions(options)
		options.addOption(Option.builder("t")
			.required(false)
			.longOpt("theme")
			.desc("Theme")
			.hasArg()
			.build())
	}

	override fun consumeCommandLine(commandLine: CommandLine) {
		super.consumeCommandLine(commandLine)
		if (commandLine.hasOption("t")) {
			Themes.setCurrent(commandLine.getOptionValue("t"))
		}
	}

	override fun createMenuBarBuilder(): MenuBarBuilder {
		return AntaresMenuBarBuilder(mainFrame, eventBus)
	}

	override fun createMainFrame(): AbstractApplicationFrame {
		// TODO Extract GraphEditor creation to factory in module
		val graphCanvas = CanvasJvm {
			val drawingView = DrawingViewImpl(GraphViewImpl<GraphElementView<*>>() as Drawing<Component>, it)
			drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
			drawingView
		}
		val graphEditor = GraphEditor(graphCanvas.view as DrawingView<Drawing<Component>>)
		val graphPanel = GraphPanel(editor = graphEditor, viewManager = viewManager)
		graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(DigitalComponentViewDrawer())

		return GraphFrame(this, graphPanel, eventBus, viewManager)
	}

	/** Implements [DesktopApplication.openFrom] by interpreting `identification` as a project name.*/
	override fun openFrom(identification: String): Boolean {
		InvocationHandler.invoke(Runnable {
			ProjectModule.projectManagementService.open(identification)
		})
		return true
	}

	override fun handleShutdown() {
		super.handleShutdown()
		if (savable is ProjectSavable) {
			BaseModule.settings.set(PROP_APPLICATION_PROJECT, (savable as ProjectSavable).project.name)
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

		close()
	}

	/** ---- [AntaresSwing] */

	private fun handle(event: OpenMemoryContentsRequest) {
		val dialog = JDialog(mainFrame, true)
		val contentsPanel = MemoryContentsPanel(
			memory = event.memory,
			addressable = event.addressable,
			cmdManager = mainFrame.editor.commandManager,
			readonly = event.readonly) { dialog.isVisible = false }
		dialog.title = Translations.getString("antares.action.memory.contents.title", event.name)
		dialog.contentPane.add(contentsPanel)
		dialog.pack()
		dialog.setLocationRelativeTo(mainFrame)
		dialog.isVisible = true
	}
}