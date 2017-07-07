package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the current [SymbolStyle] in the system.
 */
class CurrentSymbolStyle(
    initSymbolStyle: SymbolStyle,
    private var eventBus: EventBus
) {
    constructor(initSymbolStyle: SymbolStyle): this(initSymbolStyle, BaseModule.eventBus)
    constructor(): this(SymbolStyle.withName(BaseModule.properties.getString(SymbolStyle.PROP_SYMBOL_STYLE)))

    var symbolStyle: SymbolStyle = initSymbolStyle
        set(value) {
            if (field != value) {
                field = value
                BaseModule.properties.set(SymbolStyle.PROP_SYMBOL_STYLE, field.customName)
                eventBus.post(CurrentSymbolStyleChangedEvent(field))
            }
        }
}

data class CurrentSymbolStyleChangedEvent(val symbolStyle: SymbolStyle)