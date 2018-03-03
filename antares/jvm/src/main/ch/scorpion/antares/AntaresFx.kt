package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.DigitalComponentViewDrawer
import ch.scorpion.jabbah.app.AbstractDesktopApplicationFx
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.fx.ResizableCanvasFx
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.CanvasFx
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.uifx.GraphPaneFx
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.Storable
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

class AntaresFx : javafx.application.Application() {

	companion object {
		@JvmStatic fun main(args: Array<String>) {
			BaseModuleJvm.useJavaFx = true
			BaseModuleJvm.require()
			AppModule.require()
			launch(AntaresFx::class.java, *args)
		}

	}

	private lateinit var app: AntaresApplication

	/** ---- [javafx.application.Application] */

	override fun start(primaryStage: Stage?) {
		app = AntaresApplication(primaryStage!!, parameters.raw.toTypedArray())
		app.start()
	}

	override fun stop() {
		app.stop()
	}

	private inner class AntaresApplication(
		primaryStage: Stage,
		args: Array<String>
	) : AbstractDesktopApplicationFx(primaryStage, args), Antares {

		private val LOG by logger(AntaresApplication::class)

		private var editor: Editor

		private lateinit var graphPane: GraphPaneFx

		init {
			AntaresModuleJvm(this).require()

			LibraryModule.libraryHolder.library.load()
			fillStandardLibrary(LibraryModule.libraryHolder.library, IOModule.storableCreator)
			AntaresThemes.install()

			val canvas = ResizableCanvasFx()
			val canvasFx = CanvasFx(canvas, {
				EditModule.drawingViewFactory.invoke(DrawingImpl<Component>(), it)
			})
			canvas.repaintCallback = {
				LOG.debug("AntaresFX: repaintCallback")
				canvasFx.repaint()
			}
			editor = EditEditorModule.createEditor(canvasFx.view as DrawingView<Drawing<Component>>)

			BaseModule.eventBus.register(ApplicationDataEvent::class, {
				(editor.view as DrawingView<GraphView<GraphElementView<*>>>).drawing = (it.newData as MetaGraph).graph!!.graphView as GraphView<GraphElementView<*>>
			})

			fillPrimaryStage(primaryStage)

			// TODO Move to super class
			primaryStage.onCloseRequest = EventHandler<WindowEvent> {
				if (!canReplaceSavable("file.action.quit.name")) {
					it.consume()
				}
			}
		}

		fun stop() {
			shutdown()
		}

		/** ---- [Application] */

		// TODO
		override val applicationDataChanged: Boolean get() = false

		/** ---- [AbstractDesktopApplication] */

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

		override fun createNewApplicationData(): Storable {
			return MetaGraph()
		}

		override fun shutdownUI() {
			graphPane.dispose()
			super.shutdownUI()
		}

		/** ---- [AbstractDesktopApplicationFx] */

		override fun fillPrimaryStage(primaryStage: Stage) {
			val content = BorderPane()
			val menuBar = AntaresMenuBarBuilderFx(this).menuBar
			menuBar.isUseSystemMenuBar = true

			graphPane = GraphPaneFx(editor, DigitalComponentViewDrawer())
			content.top = menuBar
			content.center = graphPane.node


			primaryStage.title = displayName
			primaryStage.scene = Scene(content)
			primaryStage.show()

			DrawViewModule.viewManager.activeView = editor.view
		}
	}
}