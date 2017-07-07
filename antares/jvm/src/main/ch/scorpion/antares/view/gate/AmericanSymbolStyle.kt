package ch.scorpion.antares.view.gate

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyleChangedEvent
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * An [Action] for setting the [CurrentSymbolStyle] to [CurrentSymbolStyle.AMERICAN].
 */
class AmericanSymbolStyleAction(
        val currentSymbolStyle: CurrentSymbolStyle,
        eventBus: EventBus
) : AbstractAction("antares.action.symbolStyle.american") {

    constructor(): this(AntaresViewModule.currentSymbolStyle, BaseModule.eventBus)

    init {
        eventBus.register(CurrentSymbolStyleChangedEvent::class, { updateState() })
        updateState()
    }

    override fun actionPerformed(e: ActionEvent?) {
        currentSymbolStyle.symbolStyle = SymbolStyle.AMERICAN
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, currentSymbolStyle.symbolStyle == SymbolStyle.AMERICAN)
    }
}