package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.memory.MemoryContentsPanel
import ch.scorpion.antares.view.memory.OpenMemoryContentsRequest
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.AbstractDesktopApplication
import ch.scorpion.jabbah.app.AbstractDesktopApplicationSwing
import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.VetoException
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
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryItemRemovedEvent
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.OpenProjectRequest
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphPanel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
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
	private val viewManager: ViewManager = DrawViewModule.viewManager,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val schedulerProvider: () -> Scheduler = { ExecutionModule.scheduler }
) : AbstractDesktopApplicationSwing(args, eventBus), Antares {

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			BaseModuleJvm.require()
			AntaresSwing(args).start()
		}
	}

	init {
		eventBus.register(OpenMemoryContentsRequest::class, { handle(it) })
		eventBus.register(OpenProjectRequest::class, {
			if (!canReplaceSavable("project.action.open.name")) {
				throw VetoException()
			}
		} )
		eventBus.register(LibraryItemRemovedEvent::class, {
			if (it.item is ContainerLibraryElement && (it.item as ContainerLibraryElement).metaGraph == applicationData) {
				close()
			}
		})
	}

	/** ---- [AbstractDesktopApplication] */

	override fun createNewApplicationData(): Storable {
		return MetaGraph()
	}

	override fun init() {
		// VAqua brings too many new problems, such as property sheet not working any more
		// Temporarily disabled, looking for another solution to JToggleButton problem..
//        if (System.getProperty("os.name", "").startsWith("Mac OS")) {
//            UIManager.setLookAndFeel("org.violetlib.aqua.AquaLookAndFeel");
//        }

		AntaresModuleJvm(this).require()
		LibraryModule.libraryService.invoke().loadLibrary(LibraryModule.libraryHolder.library)
		fillStandardLibrary(LibraryModule.libraryHolder.library, LibraryModule.libraryService.invoke(), storableCreator)
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
		val graphCanvas = CanvasJvm({
			val drawingView = DrawingViewImpl(GraphViewImpl<GraphElementView<*>>() as Drawing<Component>, it)
			drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
			drawingView
		})
		val graphEditor = GraphEditor(graphCanvas.view as DrawingView<Drawing<Component>>)
		val graphPanel = GraphPanel(application = this, editor = graphEditor, viewManager = viewManager)
		graphPanel.libraryPanel.libraryPreviewPanel.addDrawableDrawer(DigitalComponentViewDrawer())

		return GraphFrame(this, graphPanel, eventBus, viewManager, schedulerProvider.invoke())
	}

	/** Implements [DesktopApplication.openFrom] by interpreting `identification` as a project name.*/
	override fun openFrom(identification: String): Boolean {
		ProjectModule.projectService.open(identification)
		return true
	}

	/** ---- [AntaresSwing] */

	private fun handle(event: OpenMemoryContentsRequest) {
		val dialog = JDialog(mainFrame, true)
		dialog.title = Translations.getString("antares.action.memory.contents.title")
		dialog.contentPane.add(
			MemoryContentsPanel(event.memory, event.addressWidth, event.dataWidth, mainFrame.editor.commandManager))
		dialog.pack()
		dialog.setLocationRelativeTo(mainFrame)
		dialog.isVisible = true
	}
}