package io.antarescircuit.jabbah.app.tip

import io.antarescircuit.jabbah.app.module.AppModule
import io.antarescircuit.jabbah.base.ui.wizard.AbstractWizardStepModel
import io.antarescircuit.jabbah.base.ui.wizard.WizardContext
import io.antarescircuit.jabbah.base.ui.wizard.WizardStepModel

data class TipOfTheDayContext(
    var showOnStartup: Boolean = true
) : WizardContext

class TipOfTheDayStep(
    val tipOfTheDay: TipOfTheDay,
    titleKey: String
) : AbstractWizardStepModel<TipOfTheDayContext>(titleKey) {

    private val nextTip: TipOfTheDay? get() =
        AppModule.tipOfTheDayProvider.getNextTip(tipOfTheDay.id)

    override val isTerminal: Boolean get() =
        !AppModule.tipOfTheDayProvider.hasNextTip(tipOfTheDay.id)

    override fun next(context: TipOfTheDayContext): WizardStepModel<TipOfTheDayContext>? =
        nextTip?.let { TipOfTheDayStep(it, it.title) }
}