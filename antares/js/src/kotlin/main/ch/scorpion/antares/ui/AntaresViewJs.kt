package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.TranslationBundleAdded
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.graphExecutionToolbar
import ch.scorpion.jabbah.graph.ui.graphNavigationView
import ch.scorpion.jabbah.graph.ui.graphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.Storable
import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface AntaresViewJsProps : RProps {
	var application: Application
	var applicationDataHolder: ApplicationDataHolder
	var canvasId: String
	var size: Dimension2D?
	var metaGraph: MetaGraph
}

external interface AntaresCanvasState : RState {
	var isLoading: Boolean
}

/** Displays simulation controls and a [graphNavigationView]. */
class AntaresViewJs(
	props: AntaresViewJsProps
) : RComponent<AntaresViewJsProps, AntaresCanvasState>(props), ApplicationDataView {

	private val translationEventHandler: EventHandler<TranslationBundleAdded> = { handle(it) }
	private val controller: GraphPanelViewController

	/** Spawns a individual [GraphApplicationContextHolder] with its separate [Scheduler] instance.*/
	private val applicationContextHolder = GraphApplicationContextHolder()

	init {
		console.info("AntaresViewJs.init")

		val drawingView = EditModule.drawingViewFactory.invoke(
			GraphViewModule.graphViewFactory.invoke(null) as Drawing<Component>,
			applicationContextHolder)

		val editor = GraphViewModule.graphEditorFactory.invoke(drawingView)

		controller = GraphPanelViewController(editor, props.applicationDataHolder, applicationContextHolder)

		// TODO Is this needed?
		GraphViewModule.applicationModeHolder = controller.applicationModeHolder


		this.state.isLoading = true
		BaseModule.eventBus.register(TranslationBundleAdded::class, translationEventHandler)
	}

	override fun dispose() {
		controller.dispose()
		BaseModule.eventBus.unregister(translationEventHandler)
	}

	private fun handle(event: TranslationBundleAdded) {
		if (hasAllBundles()) {
			setState {
				isLoading = false

				// This doesn't work yet
				//DrawViewModule.viewManager.activeView = controller.editor.view
			}
		}
	}

	private fun hasAllBundles(): Boolean {
		return Translations.hasBundle("antares")
			&& Translations.hasBundle("jabbah-base")
			&& Translations.hasBundle("jabbah-execution")
			&& Translations.hasBundle("jabbah-draw")
			&& Translations.hasBundle("jabbah-edit")
			&& Translations.hasBundle("jabbah-app")
			&& Translations.hasBundle("jabbah-graph")
	}

	/** ---- [RComponent] */

	override fun componentDidMount() {
		props.application.controller.view = this
		applicationContextHolder.scheduler.isSoftBreakpointsEnabled = true
	}

	override fun componentWillUnmount() {
		props.application.controller.dispose()
	}

	override fun RBuilder.render() {
		mCssBaseline()

		styledDiv {
			css {
				display = Display.flex
				height = 100.vh
				width = 100.vw
				flexDirection = FlexDirection.column
			}

			mAppBar(position = MAppBarPosition.static) {
				mToolbar {
					mToolbarTitle("Antares")
				}
			}

			styledDiv {
				css {
					display = Display.flex
					flexDirection = FlexDirection.column
					flexGrow = 1.0
				}
				if (state.isLoading) {
					mBackdrop(open = true) {
						mCircularProgress(color = MCircularProgressColor.inherit)
					}
				} else {
					antaresMenuBar {  }
					graphExecutionToolbar {
						applicationDataHolder = props.applicationDataHolder
						scheduler = applicationContextHolder.scheduler
						eventBus = BaseModule.eventBus
					}

					styledDiv {
						css {
							flexGrow = 1.0
							position = Position.relative
						}
						graphPanelView {
							controller = this@AntaresViewJs.controller
							application = this@AntaresViewJs.props.application
							canvasId = props.canvasId
							size = props.size
							metaGraph = props.metaGraph
						}
					}
				}
			}
		}
	}

	override fun decideSaveChangedData(action: String): SaveUnchangedDataDecision {
		// TODO
		return SaveUnchangedDataDecision.No
	}

	override fun defineSavableForStoring(storable: Storable, currentSavable: Savable?): Savable? {
		// TODO
		return null
	}

	override fun defineSavableForLoading(): Savable? {
		// TODO
		return null
	}

	override fun showModalMessage(type: ModalMessageType, title: String, message: String) {
		// TODO
	}
}