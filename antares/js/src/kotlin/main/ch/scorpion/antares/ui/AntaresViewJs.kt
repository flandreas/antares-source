package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.TranslationBundleAdded
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.graphExecutionToolbar
import ch.scorpion.jabbah.graph.ui.graphNavigationView
import ch.scorpion.jabbah.graph.ui.graphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.Storable
import com.ccfraser.muirwik.components.MCircularProgressColor
import com.ccfraser.muirwik.components.mBackdrop
import com.ccfraser.muirwik.components.mCircularProgress
import react.*
import styled.styledDiv

external interface AntaresViewJsProps : RProps {
	var application: Application
	var applicationDataHolder: ApplicationDataHolder
	var canvasId: String
	var width: Int
	var height: Int
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

	init {
		console.info("AntaresViewJs.init")

		val editor = GraphViewModule.graphEditorFactory.invoke(BaseModule.eventBus)

		controller = GraphPanelViewController(editor, props.applicationDataHolder)

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
		// TODO Check for all bundles once they all get loaded
		if (Translations.hasBundle("antares")) {
			setState {
				isLoading = false

				// This doesn't work yet
				//DrawViewModule.viewManager.activeView = controller.editor.view
			}
		}
	}

	/** ---- [RComponent] */

	override fun componentDidMount() {
		props.application.controller.view = this

		ExecutionModule.scheduler.isSoftBreakpointsEnabled = true
	}

	override fun componentWillUnmount() {
		props.application.controller.dispose()
	}

	override fun RBuilder.render() {
		styledDiv {
			if (state.isLoading) {
				mBackdrop(open = true) {
					mCircularProgress(color = MCircularProgressColor.inherit)
				}
			}
			else {
				graphExecutionToolbar {
					applicationDataHolder = props.applicationDataHolder
					scheduler = ExecutionModule.scheduler
					eventBus = BaseModule.eventBus
				}
				graphPanelView {
					controller = this@AntaresViewJs.controller
					application = this@AntaresViewJs.props.application
					canvasId = props.canvasId
					width = props.width
					height = props.height
					metaGraph = props.metaGraph
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