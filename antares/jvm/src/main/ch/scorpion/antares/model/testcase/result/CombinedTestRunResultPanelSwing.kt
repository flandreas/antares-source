package ch.scorpion.antares.model.testcase.result

import ch.scorpion.antares.model.testcase.CombinedTestRunResult
import ch.scorpion.antares.model.testcase.TestRunResult
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

class CombinedTestRunResultPanelSwing(
	results: CombinedTestRunResult?,
	private val closeHandler: (() -> Unit)? = null
) : JPanel() {

	companion object {

		val FAILED_ICON = UiUtil.themedIcon("/img/error-16.png")
		val PASSED_ICON = UiUtil.themedIcon("/img/checkmark.png")

		fun showAsDialog(
			results: CombinedTestRunResult,
			parent: Frame = Frame.getFrames()[0]
		) {
			DialogBuilder<CombinedTestRunResultPanelSwing>(parent)
				.content { dialog -> CombinedTestRunResultPanelSwing(results) { dialog.dispose()} }
				.title(Translations.getString("antares.testcase.results.title"))
				.defaultButton { it.closeButton }
				.minimumSize(Dimension(200, 200))
				.preferredSize(Dimension(400, 300))
				.show()
		}
	}

	private val tabbedPane = JTabbedPane()
	private val summaryLabel = JLabel()
	private val closeAction = CloseAction()
	private val closeButton = JButton(ActionWrapperSwing(closeAction))
	private val buttonPanel = JPanel()

	init {
		buildUI(results)
	}

	private fun buildUI(results: CombinedTestRunResult?) {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		if (closeHandler != null) {
			buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
			buttonPanel.add(Box.createHorizontalGlue())
			buttonPanel.add(closeButton)
		}

		setResults(results)
	}

	fun setResults(results: CombinedTestRunResult?) {
		removeAll()
		tabbedPane.removeAll()

		add(summaryLabel, BorderLayout.NORTH)

		results?.circuitResults?.let {
			tabbedPane.addTab(
				TestRunResult.Type.Circuit.toString(),
				getResultIcon(it),
				SingleTestRunResultPanelSwing(it))
		}
		results?.scriptResults?.let {
			tabbedPane.addTab(
				TestRunResult.Type.Script.toString(),
				getResultIcon(it),
				SingleTestRunResultPanelSwing(it))
		}

		add(tabbedPane, BorderLayout.CENTER)
		updateSummaryLabel(results)

		if (closeHandler != null) {
			add(buttonPanel, BorderLayout.SOUTH)
		}

		invalidate()
		validate()
		repaint()
	}

	private fun updateSummaryLabel(results: CombinedTestRunResult?) {
		if (results == null) {
			summaryLabel.text = ""
		}
		else if (results.error != null) {
			summaryLabel.text = results.error
		} else if (results.totalFailedCount == 0) {
			summaryLabel.text = "${results.testcase.name.value}: ${Translations.getString("antares.testcase.results.summary.passed")}"
		} else {
			summaryLabel.text = "${results.testcase.name.value}: ${Translations.getString("antares.testcase.results.summary.failed", results.totalFailedCount)}"
		}
	}

	private fun getResultIcon(result: TestRunResult): ImageIcon =
		if (result.errorMessage != null || result.failedCount > 0) {
			FAILED_ICON
		} else {
			PASSED_ICON
		}

	private inner class CloseAction : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler?.invoke()
		}
	}
}