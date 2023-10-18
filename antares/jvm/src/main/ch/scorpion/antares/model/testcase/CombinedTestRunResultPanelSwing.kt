package ch.scorpion.antares.model.testcase

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
	results: List<TestRunResult>,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val FAILED_ICON = UiUtil.themedIcon("/img/error-16.png")
		private val PASSED_ICON = UiUtil.themedIcon("/img/checkmark.png")

		fun showAsDialog(
			results: List<TestRunResult>,
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

	init {
		buildUI(results)
	}

	private fun buildUI(results: List<TestRunResult>) {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		add(summaryLabel, BorderLayout.NORTH)

		for (result in results) {
			tabbedPane.addTab(
				result.type.toString(),
				getResultIcon(result),
				TestRunResultPanelSwing(result))
		}

		add(tabbedPane, BorderLayout.CENTER)
		updateSummaryLabel(results)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(closeButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun updateSummaryLabel(results: List<TestRunResult>) {
		val totalFailedTestCount = totalFailedTestCount(results)
		if (totalFailedTestCount == 0) {
			summaryLabel.icon = PASSED_ICON
			summaryLabel.text = "${results[0].testName}: ${Translations.getString("antares.testcase.results.summary.passed")}"
		} else {
			summaryLabel.icon = FAILED_ICON
			summaryLabel.text = "${results[0].testName}: ${Translations.getString("antares.testcase.results.summary.failed", totalFailedTestCount)}"
		}
	}

	private fun totalFailedTestCount(results: List<TestRunResult>): Int =
		results.sumOf {
			if (it.errorMessage != null) {
				1
			} else {
				it.failedCount
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
			closeHandler()
		}
	}
}