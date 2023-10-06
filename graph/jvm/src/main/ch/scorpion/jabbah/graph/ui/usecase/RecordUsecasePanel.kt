package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.ColorIcon
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.drawable.SynchronizedGlowAnimation
import ch.scorpion.jabbah.draw.drawable.TransparentBridge
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
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
	private var closeHandler: () -> Unit = {}
) : JPanel() {

	companion object {

		/** Maps transparency values to the corresponding glow color. Used for reusing [Color] instances when glowing.*/
		private val recordButtonGlowColors = mutableMapOf<Int, Color>()

		fun showAsDialog(
			usecase: Usecase,
			application: Application,
			applicationModeHolder: ApplicationModeHolder,
			graphAppContextHolder: GraphApplicationContextHolder,
			service: UsecaseAppService,
			parent: Frame = Frame.getFrames()[0]
		) {
			val panel = RecordUsecasePanel(usecase.id, application, applicationModeHolder, graphAppContextHolder, service)
			DialogBuilder<RecordUsecasePanel>(parent, modal = false)
				.content { dialog ->
					panel.closeHandler = { dialog.dispose() }
					panel
				}
				.title(Translations.getString("usecase.action.record.title", usecase.name.value))
				.defaultButton { panel.okButton }
				.minimumSize(Dimension(200, 300))
				.alwaysOnTop(true)
				.show()
		}
	}

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

	private val log = StringBuilder()
	private val logView = JTextArea(log.toString())

	private val recorder = UsecaseRecorder(applicationModeHolder, ::consumeStatement, graphAppContextHolder.scheduler)

	/** The [ColorIcon] displayed on [recordButton]. Shows glow effect while in 'record' mode. */
	private val recordButtonIcon = ColorIcon(
		backgroundColor = recordColorBackground,
		foregroundColor = Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().error.foregroundColor),
		width = 15,
		height = 15,
		oval = true)
	private val recordButtonGlower = TransparentBridge(::glowRecordButton)

	init {
		buildUI()

		realtimeCheckbox.addActionListener {
			timeBetweenClicksTextField.isEnabled = !realtimeCheckbox.isSelected
		}

		delayTextField.value = BaseModule.properties.getInt(UsecaseRecorder.PROP_DEF_DELAY_MS)
		timeBetweenClicksTextField.value = BaseModule.properties.getInt(UsecaseRecorder.PROP_DEF_TIME_BETWEEN_CLICKS_MS)

		recordButton.icon = recordButtonIcon
	}

	private fun consumeStatement(statement: String) {
		if (log.isNotEmpty()) {
			log.append("\n")
		}
		log.append(statement)
		logView.text = log.toString()
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

		panel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(panel, okButton, cancelButton)

		return panel
	}

	private fun startRecording() {
		okAction.enabled = false
		cancelAction.enabled = false
		recordAction.name = Translations.getString("usecase.action.record.stop.name")
		recorder.start(
			realtimeCheckbox.isSelected,
			delayTextField.value as Int * 1_000,
			timeBetweenClicksTextField.value as Int * 1_000)
		SynchronizedGlowAnimation.add(recordButtonGlower)
	}

	private fun stopRecording() {
		recorder.stop()
		okAction.enabled = true
		cancelAction.enabled = true
		recordAction.name = Translations.getString("usecase.action.record.start.name")
		SynchronizedGlowAnimation.remove(recordButtonGlower)
	}

	/** Handler of glow animation ticks to make the [recordButtonIcon] glow.*/
	private fun glowRecordButton(transparency: Int) {
		recordButtonIcon.backgroundColor = recordButtonGlowColors.getOrPut(transparency) {
			Color(recordColorBackground.red, recordColorBackground.green, recordColorBackground.blue, transparency)
		}
		recordButton.invalidate()
		recordButton.repaint()
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
		}
	}
}