package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.app.ApplicationDataViewJs
import ch.scorpion.jabbah.app.action.SaveFileAction
import ch.scorpion.jabbah.base.TranslationBundleAdded
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.mreact.jmButton
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.util.decodeURI
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
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.browser.window
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface AntaresViewJsProps : Props {
	var application: Application
	var applicationDataHolder: ApplicationDataHolder
	var canvasId: String
	var size: Dimension2D?
	var projectName: String?
	var metaGraph: MetaGraph?
	var returnUri: String?
}

external interface AntaresCanvasState : State {
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

	private var saveAction: SaveFileAction? = null

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

		this.state.isLoading = !hasAllBundles()
		BaseModule.eventBus.register(TranslationBundleAdded::class, translationEventHandler)
	}

	fun dispose() {
		props.application.controller.dispose()
		controller.dispose()
		saveAction?.dispose()
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
		dispose()
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
					mAppBar(position = MAppBarPosition.static) {
						mToolbar {
							if (props.projectName != null) {
								mToolbarTitle("Antares Desktop - ${props.projectName}")
							} else {
								mToolbarTitle("Antares Desktop")
							}
							saveAction = SaveFileAction(props.application, applicationContextHolder.eventBus)
							jmButton(saveAction!!)
							props.returnUri?.let { returnUri ->
								mButton("Close", color = MColor.inherit, variant = MButtonVariant.outlined, size = MButtonSize.small,
									onClick = { window.location.href = decodeURI(returnUri) }) {
									css {
										marginLeft = 10.px
									}
								}
							}
						}
					}

					antaresMenuBar {  }
					graphExecutionToolbar {
						currentSystemSpeedCategory = controller.applicationContextHolder.currentSystemSpeedCategory
						scheduler = applicationContextHolder.scheduler
						eventBus = BaseModule.eventBus
						toggleApplicationModeAction = controller.toggleApplicationModeAction
						singleStepModeAction = controller.singleStepModeAction
						pauseOrResumeAction = controller.pauseOrResumeAction
						backgroundColor = null
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