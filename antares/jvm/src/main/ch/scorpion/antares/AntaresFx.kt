package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.jabbah.app.AbstractDesktopApplicationFx
import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.uifx.GraphUIFx
import ch.scorpion.jabbah.io.Storable
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

class AntaresFx : javafx.application.Application() {

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
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

		private var ui: GraphUIFx

		init {
			AntaresModuleJvm(this).require()

			LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(LibraryModule.libraryHolder.library.uuid)
			AntaresThemes.install()

			ui = GraphUIFx(this, AntaresMenuBarBuilderFx(this))

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
			ui.dispose()
			super.shutdownUI()
		}

		/** ---- [AbstractDesktopApplicationFx] */

		override fun fillPrimaryStage(primaryStage: Stage) {
			primaryStage.title = displayName
			primaryStage.scene = Scene(ui.node)
			primaryStage.show()
		}
	}
}