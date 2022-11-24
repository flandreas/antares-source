package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the current [SymbolStyle] in the system.
 */
class CurrentSymbolStyle(
    initSymbolStyle: SymbolStyle,
    private var eventBus: EventBus = BaseModule.eventBus
) {
    /** Initializes [CurrentSymbolStyle] with the [SymbolStyle] stored in the [Properties].*/
    constructor(): this(SymbolStyle.withName(BaseModule.properties.getString(SymbolStyle.PROP_SYMBOL_STYLE)))

    var symbolStyle: SymbolStyle = initSymbolStyle
        set(value) {
            if (field != value) {
                field = value
	            BaseModule.properties.customize(SymbolStyle.PROP_SYMBOL_STYLE, field.customName)
                eventBus.post(CurrentSymbolStyleChangedEvent(field))
            }
        }

	init {
		eventBus.register(PreferencesChangedEvent::class) {
			symbolStyle = SymbolStyle.withName(BaseModule.properties.getString(SymbolStyle.PROP_SYMBOL_STYLE))
		}
	}
}

data class CurrentSymbolStyleChangedEvent(val symbolStyle: SymbolStyle)

class CurrentSymbolStyleToString(val map: Map<SymbolStyle, String>) {
	fun evaluate(): String =
		map[AntaresViewModule.currentSymbolStyle.symbolStyle] ?: throw IllegalStateException("unmapped SymbolStyle")
}