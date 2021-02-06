package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.TranslationBundleAdded
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.ui.graphExecutionToolbar
import ch.scorpion.jabbah.graph.ui.graphNavigationView
import ch.scorpion.jabbah.graph.ui.graphPanelView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import com.ccfraser.muirwik.components.MCircularProgressColor
import com.ccfraser.muirwik.components.mBackdrop
import com.ccfraser.muirwik.components.mCircularProgress
import react.*
import styled.styledDiv

external interface AntaresCanvasProps : RProps {
	var application: Application
	var applicationDataHolder: ApplicationDataHolder
	var canvasId: String
	var width: Int
	var height: Int
	var drawing: GraphView
}

external interface AntaresCanvasState : RState {
	var isLoading: Boolean
}

/** Displays simulation controls and a [graphNavigationView]. */
class AntaresCanvas(props: AntaresCanvasProps) : RComponent<AntaresCanvasProps, AntaresCanvasState>(props) {

	private val editor: Editor
	private val applicationModeHolder: ApplicationModeHolder
	private val translationEventHandler: EventHandler<TranslationBundleAdded> = { handle(it) }

	init {
		editor = GraphViewModule.graphEditorFactory.invoke(BaseModule.eventBus)
		applicationModeHolder = ApplicationModeHolderImpl(editor)
		GraphViewModule.applicationModeHolder = applicationModeHolder

		this.state.isLoading = true
		BaseModule.eventBus.register(TranslationBundleAdded::class, translationEventHandler)
	}

	private fun handle(event: TranslationBundleAdded) {
		// TODO Check for all bundles once they all get loaded
		if (Translations.hasBundle("antares")) {
			setState {
				isLoading = false
			}
		}
	}

	override fun componentDidMount() {
		DrawViewModule.viewManager.activeView = editor.view

		ExecutionModule.scheduler.isSoftBreakpointsEnabled = true

		// In absence of a real application controller. Used to enable ToggleApplicationModeAction
		BaseModule.eventBus.post(ApplicationDataEvent(null, ApplicationData(props.drawing, DefaultSavable("Web"))))
	}

	override fun componentWillUnmount() {
		applicationModeHolder.dispose()
		BaseModule.eventBus.unregister(translationEventHandler)
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
					canvasId = props.canvasId
					width = props.width
					height = props.height
					drawing = props.drawing
					editor = this@AntaresCanvas.editor
					application = this@AntaresCanvas.props.application
					applicationModeHolder = this@AntaresCanvas.applicationModeHolder
				}
			}
		}
	}
}