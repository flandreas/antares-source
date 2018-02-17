package ch.scorpion.jabbah.app.drawingapp

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.SystemJvm
import ch.scorpion.jabbah.base.geom.AffineTransformFx
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.ThemeEvent
import ch.scorpion.jabbah.draw.view.CanvasFx
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.io.Storable
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.WindowEvent

class DrawingApplication : javafx.application.Application() {

	companion object {
		private val FILE_EXTENSION = "draw"

		@JvmStatic fun main(args: Array<String>) {
			EditModuleJvm.require()
			AppModule.require()
			System.SYSTEM = SystemJvm(useJavaFX = true)

			launch(DrawingApplication::class.java, *args)
		}
	}

	private lateinit var app: DrawingApplicationJabbah

	/** ---- [javafx.application.Application] */

	override fun start(primaryStage: Stage?) {
		app = DrawingApplicationJabbah(primaryStage!!, parameters.raw.toTypedArray())
		app.start()
	}

	override fun stop() {
		app.stop()
	}

	/** ---- [DrawingApplication] */

	private inner class DrawingApplicationJabbah(
		primaryStage: Stage,
		args: Array<String>
	) : AbstractDesktopApplicationFx(primaryStage, args) {

		private var editor: Editor

		init {
			val canvas = Canvas()
			val canvasFx = CanvasFx(canvas, { DrawingViewImpl(DrawingImpl<Component>(), it, { AffineTransformFx() }) })
			editor = EditEditorModule.createEditor(canvasFx.view as DrawingView<Drawing<Component>>)

			BaseModule.eventBus.register(ApplicationDataEvent::class, {
				editor.view.drawing = it.newData as Drawing<Component>
			})
			fillPrimaryStage(primaryStage)

			primaryStage.onCloseRequest = EventHandler<WindowEvent> {
				if (!canReplaceSavable("file.action.quit.name")) {
					it.consume()
				}
			}
		}

		/** ---- [Application] */

		override val displayName: String get() = "Drawing Application"

		override val applicationDataChanged: Boolean get() = editor.commandManager.canUndo()

		override val fileExtension: String get() = FILE_EXTENSION

		override fun fillPrimaryStage(primaryStage: Stage) {
			val content = VBox()
			val menuBar = MenuBarBuilderFx(this).menuBar
			val toolBar = ToolBarBuilderFx(editor).build()

			(editor.view.canvas as CanvasFx).canvas.widthProperty().bind(content.widthProperty())
			(editor.view.canvas as CanvasFx).canvas.heightProperty().bind(
				content.heightProperty().subtract(menuBar.heightProperty().add(toolBar.heightProperty())))

			(editor.view.canvas as CanvasFx).canvas.widthProperty().addListener { _ -> editor.view.repaint() }
			(editor.view.canvas as CanvasFx).canvas.heightProperty().addListener { _ -> editor.view.repaint() }

			val propertyPane = ComponentPropertyPaneFx(editor)
			propertyPane.padding = Insets(10.0, 10.0, 10.0, 10.0)
			val scrollPane = ScrollPane(propertyPane)
			scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
			scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
			scrollPane.prefViewportWidth = 350.0

			val holder = BorderPane()
			holder.left = scrollPane
			holder.center = (editor.view.canvas as CanvasFx).canvas

			BackgroundInstaller(DrawStyleModule.styleProvider, holder)
			content.children.addAll(menuBar, toolBar, holder)

			primaryStage.title = displayName
			primaryStage.scene = Scene(content)
			primaryStage.show()
			editor.view.navigator.fitMaxNormal()
			DrawViewModule.viewManager.activeView = editor.view
		}

		override fun createNewApplicationData(): Storable {
			return DrawingImpl<Component>()
		}

		/** ---- [DrawingApplicationJabbah] */

		fun stop() {
			shutdown()
		}

		private inner class BackgroundInstaller(private val styleProvider: StyleProvider, private val region: Region) {

			init {
				BaseModule.eventBus.register(ThemeEvent::class, { installBackgroundColor() })
				installBackgroundColor()
			}

			private fun installBackgroundColor() {
				val color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
				region.background = Background(BackgroundFill(Color.rgb(color.red, color.green, color.blue), CornerRadii.EMPTY, Insets.EMPTY))
			}
		}
	}
}