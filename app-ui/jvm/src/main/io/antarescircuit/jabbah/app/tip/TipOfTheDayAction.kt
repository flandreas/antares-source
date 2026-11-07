package io.antarescircuit.jabbah.app.tip

import io.antarescircuit.jabbah.app.module.AppModule
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.ui.wizard.WizardController
import io.antarescircuit.jabbah.base.ui.wizard.WizardViewSwing
import java.awt.Frame
import javax.swing.JCheckBox
import javax.swing.JOptionPane

/**
 * [Action] for displaying a [WizardViewSwing] with the "tips of the day"
 */
class TipOfTheDayAction : AbstractAction("application.tipOfTheDay.action") {

    companion object {

        fun showWizard() {
            AppModule.tipOfTheDayProvider.getNextTip()?.let { tip ->
                val context = TipOfTheDayContext()
                val controller = WizardController(TipOfTheDayStep(tip, tip.title), context, supportFinish = false)

                val showOnStartup = JCheckBox(Translations.getString("application.tipOfTheDay.showOnStartup.text"))
                showOnStartup.isSelected = AppModule.tipOfTheDayProvider.showOnStartup

                WizardViewSwing.showAsDialog(
                    Translations.getString("application.tipOfTheDay.action.name"),
                    controller,
                    viewFactory = { step, _ -> TipOfTheDayPanel((step as TipOfTheDayStep).tipOfTheDay) },
                    parent = Frame.getFrames()[0],
                    customButtonBarComponent = showOnStartup
                )

                AppModule.tipOfTheDayProvider.showOnStartup = showOnStartup.isSelected
            }
        }
    }

    override fun execute(event: ActionEvent) {
        if (!AppModule.tipOfTheDayProvider.hasNextTip()) {
            if (JOptionPane.showConfirmDialog(
                    Frame.getFrames()[0],
                    Translations.getString("application.tipOfTheDay.noMoreTips.text"),
                    Translations.getString("application.tipOfTheDay.action.name"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                ) == JOptionPane.YES_OPTION) {
                AppModule.tipOfTheDayProvider.resetCurrentId()
            } else {
                return
            }
        }

        showWizard()
    }
}