package io.antarescircuit.jabbah.base.ui.wizard

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.base.ui.UIView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

/** Creates the Swing component used to display one wizard step. */
fun interface SwingWizardStepViewFactory<C : WizardContext> {
	fun create(step: WizardStepModel<C>, controller: WizardController<C>): JComponent
}

/**
 * Reusable Swing content panel for a [WizardController].
 *
 * @property dialog if not `null`, that [JDialog] is packed each time the current wizard step changes.
 */
class WizardViewSwing<C : WizardContext>(
	private val controller: WizardController<C>,
	private val viewFactory: SwingWizardStepViewFactory<C>,
	private val dialog: JDialog?,
	private val customButtonBarComponent: JComponent? = null,
	private val closeHandler: () -> Unit,
) : JPanel(BorderLayout(0, UIBasics.ROW_GAP)), WizardView {

	companion object {

		/**
		 * Convenience API for displaying a wizard as a modal dialog.
		 *
		 * @param preferredSize determines the size of the dialog if set. If `null`, the dialog will be packed
		 * each time the current wizard step changes.
		 */
		fun <C : WizardContext> showAsDialog(
			title: String,
			controller: WizardController<C>,
			viewFactory: SwingWizardStepViewFactory<C>,
			preferredSize: Dimension? = null,
			parent: Frame? = null,
			customButtonBarComponent: JComponent? = null
		): C? {
			val builder = DialogBuilder<WizardViewSwing<C>>(parent)
				.content { dialog ->
					WizardViewSwing(
						controller,
						viewFactory,
						if (preferredSize == null) dialog else null,
						customButtonBarComponent
					) { dialog.dispose() }
				}
				.title(title)
				.defaultButton { it.nextButton }
				.minimumSize(Dimension(300, 200))
				.onWindowClosed {
					if (!controller.completed) controller.cancel()
					controller.dispose()
				}

			preferredSize?.let { builder.preferredSize(it) }

			builder.show()

			return controller.result
		}
	}

	private val titleLabel = JLabel()
	private val errorLabel = JLabel()
	private val stepPanel = JPanel(BorderLayout())

	private val backSwingAction = ActionWrapperSwing(controller.backAction)
	private val nextSwingAction = ActionWrapperSwing(controller.nextAction)
	private val finishSwingAction = ActionWrapperSwing(controller.finishAction)
	private val cancelSwingAction = ActionWrapperSwing(controller.cancelAction)

	private val backButton = JButton(backSwingAction)
	private val nextButton = JButton(nextSwingAction)
	private val finishButton = JButton(finishSwingAction)
	private val cancelButton = JButton(cancelSwingAction)

	private var currentView: JComponent? = null
	private var displayedStep: WizardStepModel<C>? = null

	init {
		border = UIBasics.createDialogBorder()
		errorLabel.foreground = java.awt.Color.RED
		buildUi()
		controller.view = this
	}

	override fun dispose() {
		backSwingAction.dispose()
		nextSwingAction.dispose()
		finishSwingAction.dispose()
		cancelSwingAction.dispose()
		(currentView as? UIView)?.dispose()
	}

	private fun buildUi() {
		titleLabel.font = UIBasics.HEADER_FONT
		stepPanel.border = BorderFactory.createEmptyBorder(0, 0, UIBasics.ROW_GAP, 0)

		val header = JPanel()
		header.layout = BoxLayout(header, BoxLayout.PAGE_AXIS)
		header.add(titleLabel)
		header.add(Box.createVerticalStrut(UIBasics.ROW_GAP))
		header.add(errorLabel)

		val buttons = JPanel()
		buttons.layout = BoxLayout(buttons, BoxLayout.LINE_AXIS)

		customButtonBarComponent?.let {
			buttons.add(it)
			buttons.add(Box.createHorizontalStrut(30))
		}
		buttons.add(Box.createHorizontalGlue())
		buttons.add(cancelButton)
		buttons.add(Box.createHorizontalStrut(UIBasics.BUTTON_GROUP_GAP))
		buttons.add(backButton)
		buttons.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttons.add(nextButton)
		if (controller.supportFinish) {
			buttons.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
			buttons.add(finishButton)
		}

		add(header, BorderLayout.NORTH)
		add(stepPanel, BorderLayout.CENTER)
		add(buttons, BorderLayout.SOUTH)

		SwingUtilities.invokeLater {
			nextButton.requestFocusInWindow()
		}
	}

	override fun update() {
		if (controller.completed || controller.cancelled) {
			closeHandler()
			return
		}
		titleLabel.text = Translations.getOptionalString(controller.currentStep.titleKey)
			?: controller.currentStep.titleKey
		val error = controller.validationError
		errorLabel.text = error?.let {
			Translations.getOptionalString(it.messageKey) ?: it.messageKey
		} ?: ""
		if (displayedStep !== controller.currentStep) {
			val newView = viewFactory.create(controller.currentStep, controller)
			(currentView as? UIView)?.dispose()
			currentView = newView
			displayedStep = controller.currentStep

			stepPanel.removeAll()
			stepPanel.add(newView, BorderLayout.CENTER)

			if (dialog != null) {
				dialog.pack()
			} else {
				stepPanel.revalidate()
				stepPanel.repaint()
			}
		}
	}
}
