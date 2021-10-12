package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.app.ApplicationDataViewJs
import ch.scorpion.jabbah.base.TranslationBundleAdded
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.graphExecutionToolbar
import ch.scorpion.jabbah.graph.ui.graphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface AntaresViewJsProps : Props {
	var application: Application
	var applicationDataHolder: ApplicationDataHolder
	var canvasId: String
	var size: Dimension2D?
	var metaGraph: MetaGraph
}

external interface AntaresCanvasState : RState {
	var isLoading: Boolean
}

/**
 * This is roughly equivalent to [GraphFrame] of the desktop version, but still without Controller/View separation,
 * and still without a Container view.
 */
class AntaresViewJs(
	props: AntaresViewJsProps
) : RComponent<AntaresViewJsProps, AntaresCanvasState>(props) {

	private val translationEventHandler: EventHandler<TranslationBundleAdded> = { handle(it) }
	private val controller: GraphPanelViewController
	private val systemSpeed = SystemSpeed()
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed)
	private val scheduler = SchedulerImpl(currentSystemSpeedCategory)
	private val applicationDataView = ApplicationDataViewJs()

	/** Spawns a individual [GraphApplicationContextHolder] with its separate [Scheduler] instance.*/
	private val applicationContextHolder = GraphApplicationContextHolder(scheduler, systemSpeed = systemSpeed, currentSystemSpeedCategory = currentSystemSpeedCategory)

	init {
		console.info("AntaresViewJs.init")

		val drawingView = EditModule.drawingViewFactory.create(
			GraphViewModule.graphViewFactory.invoke(null) as Drawing<Component>,
			applicationContextHolder,
			displayGlobalMessages = true)

		val editor = GraphViewModule.graphEditorFactory.invoke(drawingView)

		val applicationModeHolder = ApplicationModeHolderImpl(editor, scheduler).also {
			applicationContextHolder.applicationModeHolder = it
		}

		controller = GraphPanelViewController(editor, props.applicationDataHolder, applicationContextHolder, applicationModeHolder)

		this.state.isLoading = true
		BaseModule.eventBus.register(TranslationBundleAdded::class, translationEventHandler)
	}

	fun dispose() {
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
		props.application.controller.view = applicationDataView
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
						scheduler = applicationContextHolder.scheduler
						eventBus = BaseModule.eventBus
						toggleApplicationModeAction = controller.toggleApplicationModeAction
						currentSystemSpeedCategory = controller.applicationContextHolder.currentSystemSpeedCategory
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
}