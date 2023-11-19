package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.ColorIcon
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.drawable.SynchronizedGlowAnimation
import ch.scorpion.jabbah.draw.drawable.TransparentBridge
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.app.UsecaseAppService
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRecorder
import java.awt.*
import java.text.DecimalFormat
import javax.swing.*
import javax.swing.text.NumberFormatter

class RecordUsecasePanel(
	private val usecaseId: Int,
	private val application: Application,
	applicationModeHolder: ApplicationModeHolder,
	graphAppContextHolder: GraphApplicationContextHolder,
	private val service: UsecaseAppService,
	private val eventBus: EventBus = BaseModule.eventBus,
	private var closeHandler: () -> Unit = {}
) : JPanel() {

	companion object {

		/** Maps transparency values to the corresponding glow color. Used for reusing [Color] instances when glowing.*/
		private val recordButtonGlowColors = mutableMapOf<Int, Color>()

		private const val RECORD_BUTTON_INSET = 4

		fun showAsDialog(
			usecase: Usecase,
			application: Application,
			applicationModeHolder: ApplicationModeHolder,
			graphAppContextHolder: GraphApplicationContextHolder,
			service: UsecaseAppService,
			parent: Frame = Frame.getFrames()[0]
		) {
			val panel = RecordUsecasePanel(usecase.id, application, applicationModeHolder, graphAppContextHolder, service)
			DialogBuilder<RecordUsecasePanel>(parent, modal = true)
				.content { dialog ->
					panel.dialog = dialog
					panel.closeHandler = { dialog.dispose() }
					panel
				}
				.title(Translations.getString("usecase.action.record.title", usecase.name.value))
				.defaultButton { panel.okButton }
				.minimumSize(Dimension(200, 300))
				.alwaysOnTop(true)
				.preventWindowClose(true)
				.onWindowClosed { it.dispose() }
				.show()
		}
	}

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		if (it.applicationMode == ApplicationMode.EDIT) {
			stopRecording()
		}
	}

	private val idleColor = ch.scorpion.jabbah.draw.graphics.Color.DARK_GRAY
	private val recordColorBackground = Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().error.backgroundColor)

	private val formatter = NumberFormatter(DecimalFormat.getIntegerInstance()).apply {
		minimum = 1
	}

	/** Choose whether recording should happen in realtime or in 'condensed' (faster) time.*/
	private val realtimeCheckbox = JCheckBox()

	/** Allows the user to enter the delay between 'pressed' and 'released' events (in ms). */
	private val delayTextField = JFormattedTextField(formatter)

	/** Allows the user to enter the time between two actions (in ms). Only enabled if [realtimeCheckbox] is not selected. */
	private val timeBetweenClicksTextField = JFormattedTextField(formatter)

	/** Starts and stops recording. */
	private val recordAction = RecordAction()
	private val recordButton = JButton(ActionWrapperSwing(recordAction))

	private val okAction = OKAction()
	private val okButton = JButton(ActionWrapperSwing(okAction))
	private val cancelAction = CancelAction()
	private val cancelButton = JButton(ActionWrapperSwing(cancelAction))
	private val helpAction = HelpAction(UsecaseRecorder.HELP_ID)

	private val log = StringBuilder()
	private val logView = JTextArea(log.toString())

	private val recorder = UsecaseRecorder(applicationModeHolder, ::consumeStatement, graphAppContextHolder.scheduler)

	/** The [ColorIcon] displayed on [recordButton]. Shows glow effect while in 'record' mode. */
	private val recordButtonIcon = ColorIcon(
		width = recordButton.preferredSize.height - 2 * RECORD_BUTTON_INSET,
		height = recordButton.preferredSize.height - 2 * RECORD_BUTTON_INSET,
		oval = true)
	private val recordButtonGlower = TransparentBridge(::glowRecordButton)

	/** Used for changing modality while dialog is open, i.e. make it non-modal once recording is started.*/
	private lateinit var dialog: JDialog

	init {
		buildUI()

		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)

		realtimeCheckbox.addActionListener { updateFields() }

		delayTextField.value = BaseModule.properties.getInt(UsecaseRecorder.PROP_DEF_DELAY_MS)
		timeBetweenClicksTextField.value = BaseModule.properties.getInt(UsecaseRecorder.PROP_DEF_TIME_BETWEEN_CLICKS_MS)

		recordButton.icon = recordButtonIcon
		updateRecordIcon()
	}

	private fun dispose() {
		eventBus.unregister(applicationModeHandler)
	}

	private fun consumeStatement(statement: String) {
		if (log.isNotEmpty()) {
			log.append("\n")
		}
		log.append(statement)
		logView.text = log.toString()
	}

	private fun resetLog() {
		log.clear()
		logView.text = log.toString()
	}

	private fun updateFields() {
		realtimeCheckbox.isEnabled = !recorder.isRecording
		delayTextField.isEnabled = !recorder.isRecording
		timeBetweenClicksTextField.isEnabled = !realtimeCheckbox.isSelected && !recorder.isRecording
	}

	private fun updateRecordIcon() {
		if (recorder.isRecording) {
			recordButtonIcon.backgroundColor = recordColorBackground
			recordButtonIcon.foregroundColor = Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().error.foregroundColor)
		} else {
			recordButtonIcon.backgroundColor = Graphics2DJvm.toAwtColor(idleColor)
			recordButtonIcon.foregroundColor = Graphics2DJvm.toAwtColor(idleColor.darker())
		}
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		add(buildContentPanel(), BorderLayout.CENTER)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	private fun buildContentPanel(): JComponent {
		val panel = JPanel(BorderLayout())

		val header = JPanel()
		header.layout = BoxLayout(header, BoxLayout.PAGE_AXIS)
		recordButton.alignmentX = Component.LEFT_ALIGNMENT
		header.add(buildFieldsPanel())
		header.add(Box.createVerticalStrut(5))
		panel.add(header, BorderLayout.NORTH)

		logView.isEditable = false
		logView.rows = 20
		logView.columns = 40
		val logScroll = JScrollPane(logView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
		logScroll.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(logScroll, BorderLayout.CENTER)

		return panel
	}

	private fun buildFieldsPanel(): JComponent {
		val inset = 5
		var row = 0
		val rowDist = 5
		val panel = JPanel(EGBL.getLayout())

		delayTextField.columns = 10
		timeBetweenClicksTextField.columns = 10

		// Realtime Checkbox
		EGBL.add(
			panel,
			JLabel(Translations.getString("usecase.action.record.realtime")),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, inset, 0, 0
		)
		EGBL.add(
			panel,
			realtimeCheckbox,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			0, 10, 0, inset
		)

		// Delay TextField
		EGBL.add(
			panel,
			JLabel(Translations.getString("usecase.action.record.delay")),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0
		)
		EGBL.add(
			panel,
			delayTextField,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset
		)

		// TimeBetweenClicks TextField
		EGBL.add(
			panel,
			JLabel(Translations.getString("usecase.action.record.timeBetweenClicks")),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0
		)
		EGBL.add(
			panel,
			timeBetweenClicksTextField,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset
		)

		// Record Button
		EGBL.add(
			panel,
			recordButton,
			0, row,    // x, y
			EGBL.REMAINDER, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			15, inset, 0, 0
		)

		// Filler

		val filler = JPanel()
		EGBL.add(
			panel,
			filler,
			10, ++row,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH
		)

		return panel
	}

	private fun buildButtonPanel(): JComponent {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		panel.add(UiUtil.createToolBarButton(helpAction))
		panel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(panel, okButton, cancelButton)

		return panel
	}

	private fun startRecording() {
		resetLog()
		changeModality(false)
		okAction.enabled = false
		cancelAction.enabled = false
		recordAction.name = Translations.getString("usecase.action.record.stop.name")
		recorder.start(
			realtimeCheckbox.isSelected,
			delayTextField.value as Int * 1_000,
			timeBetweenClicksTextField.value as Int * 1_000)
		updateRecordIcon()
		SynchronizedGlowAnimation.add(recordButtonGlower)
	}

	private fun stopRecording() {
		if (recorder.isRecording) {
			recorder.stop()
			okAction.enabled = true
			cancelAction.enabled = true
			recordAction.name = Translations.getString("usecase.action.record.start.name")
			SynchronizedGlowAnimation.remove(recordButtonGlower)
			updateRecordIcon()
			changeModality(true)
		}
	}

	/** Handler of glow animation ticks to make the [recordButtonIcon] glow.*/
	private fun glowRecordButton(transparency: Int) {
		recordButtonIcon.backgroundColor = recordButtonGlowColors.getOrPut(transparency) {
			Color(recordColorBackground.red, recordColorBackground.green, recordColorBackground.blue, transparency)
		}
		recordButton.invalidate()
		recordButton.repaint()
	}

	private fun changeModality(modal: Boolean) {
		SwingUtilities.invokeLater {
			dialog.isVisible = false
			dialog.isModal = modal
			dialog.isVisible = true
		}
	}

	private inner class OKAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			service.recordUsecase(application, usecaseId, log.toString())
			closeHandler.invoke()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private inner class RecordAction : AbstractAction("usecase.action.record.start") {
		override fun execute(event: ActionEvent) {
			if (recorder.isRecording) {
				stopRecording()
			} else {
				startRecording()
			}
			updateFields()
		}
	}
}