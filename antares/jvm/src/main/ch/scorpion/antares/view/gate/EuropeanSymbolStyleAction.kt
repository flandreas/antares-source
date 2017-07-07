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
 * An [Action] for setting the [CurrentSymbolStyle] to [CurrentSymbolStyle.EUROPEAN].
 */
class EuropeanSymbolStyleAction(
    val currentSymbolStyle: CurrentSymbolStyle,
    eventBus: EventBus
) : AbstractAction("antares.action.symbolStyle.european") {

    constructor(): this(AntaresViewModule.currentSymbolStyle, BaseModule.eventBus)

    init {
        eventBus.register(CurrentSymbolStyleChangedEvent::class, { updateState() })
        updateState()
    }

    override fun actionPerformed(e: ActionEvent?) {
        currentSymbolStyle.symbolStyle = SymbolStyle.EUROPEAN
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, currentSymbolStyle.symbolStyle == SymbolStyle.EUROPEAN)
    }
}