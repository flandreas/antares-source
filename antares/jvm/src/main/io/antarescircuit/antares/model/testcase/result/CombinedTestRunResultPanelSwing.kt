package io.antarescircuit.antares.model.testcase.result

import io.antarescircuit.antares.model.testcase.CombinedTestRunResult
import io.antarescircuit.antares.model.testcase.TestRunResult
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

class CombinedTestRunResultPanelSwing(
	results: CombinedTestRunResult?
) : JPanel() {

	companion object {

		val FAILED_ICON = UiUtil.themedIcon("/img/error-16.png")
		val PASSED_ICON = UiUtil.themedIcon("/img/checkmark.png")
		val IGNORED_ICON = UiUtil.themedIcon("/img/testcase.png")
	}

	private val tabbedPane = JTabbedPane()
	private val summaryLabel = JLabel()

	init {
		buildUI(results)
	}

	private fun buildUI(results: CombinedTestRunResult?) {
		layout = BoxLayout(this, BoxLayout.Y_AXIS)
		border = BorderFactory.createEmptyBorder(UIBasics.DIALOG_BORDER, UIBasics.DIALOG_BORDER, 0, UIBasics.DIALOG_BORDER)

		setResults(results)
	}

	fun setResults(results: CombinedTestRunResult?) {
		removeAll()
		tabbedPane.removeAll()

		if (results == null) {
			invalidate()
			validate()
			repaint()
			return
		}

		updateSummaryLabel(results)

		results.circuitResults?.let {
			tabbedPane.addTab(
				TestRunResult.Type.Circuit.toString(),
				getResultIcon(it),
				SingleTestRunDataResultPanelSwing(it),
				Translations.getString("antares.testcase.result.type.circuit.desc"))
		}
		results.scriptResults?.let {
			tabbedPane.addTab(
				TestRunResult.Type.Script.toString(),
				getResultIcon(it),
				SingleTestRunDataResultPanelSwing(it),
				Translations.getString("antares.testcase.result.type.script.desc"))
		}

		summaryLabel.alignmentX = Component.LEFT_ALIGNMENT
		add(summaryLabel)
		add(Box.createVerticalStrut(10))

		if (results.generalTestResults.isNotEmpty()) {
			val generalSectionSeparator = JSeparator(JSeparator.HORIZONTAL)
			generalSectionSeparator.preferredSize = Dimension(Int.MAX_VALUE, 10)
			generalSectionSeparator.alignmentX = Component.LEFT_ALIGNMENT
			add(generalSectionSeparator)
			val generalTitle = JLabel(Translations.getString("antares.testcase.generalTest.results.title"))
			generalTitle.alignmentX = Component.LEFT_ALIGNMENT
			add(generalTitle)

			val generalContent = SingleTestRunGeneralResultPanelSwing(results.generalTestResults)
			generalContent.alignmentX = Component.LEFT_ALIGNMENT
			add(generalContent)
		}

		add(Box.createVerticalStrut(10))
		val dataSectionSeparator = JSeparator(JSeparator.HORIZONTAL)
		dataSectionSeparator.preferredSize = Dimension(Int.MAX_VALUE, 10)
		dataSectionSeparator.alignmentX = Component.LEFT_ALIGNMENT
		add(dataSectionSeparator)
		val dataTitle = JLabel(Translations.getString("antares.testcase.dataTest.results.title"))
		dataTitle.alignmentX = Component.LEFT_ALIGNMENT
		add(dataTitle)
		tabbedPane.alignmentX = Component.LEFT_ALIGNMENT
		add(tabbedPane)

		invalidate()
		validate()
		repaint()
	}

	private fun updateSummaryLabel(results: CombinedTestRunResult?) {
		if (results == null) {
			summaryLabel.icon = null
			summaryLabel.text = ""
		}
		else if (results.error != null && !results.ignored) {
			summaryLabel.icon = FAILED_ICON
			summaryLabel.text = results.error
		} else if (results.totalFailedCount == 0) {
			summaryLabel.icon = null
			summaryLabel.text = "${results.testcase.name.value}: ${Translations.getString("antares.testcase.results.summary.passed")}"
		} else {
			summaryLabel.icon = null
			summaryLabel.text = "${results.testcase.name.value}: ${Translations.getString("antares.testcase.results.summary.failed", results.totalFailedCount)}"
		}
	}

	private fun getResultIcon(result: TestRunResult): ImageIcon =
		if (result.errorMessage != null || result.failedCount > 0) {
			FAILED_ICON
		} else {
			PASSED_ICON
		}
}