package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the current [SymbolStyle] in the system.
 */
class CurrentSymbolStyle(
    initSymbolStyle: SymbolStyle,
) {
    /** Initializes [CurrentSymbolStyle] with the [SymbolStyle] stored in the [Properties].*/
    constructor(): this(SymbolStyle.withName(BaseModule.properties.getString(SymbolStyle.PROP_SYMBOL_STYLE)))

    val symbolStyle: SymbolStyle = initSymbolStyle
}

class CurrentSymbolStyleToString(val map: Map<SymbolStyle, String>) {
	fun evaluate(): String =
		map[AntaresViewModule.currentSymbolStyle.symbolStyle] ?: throw IllegalStateException("unmapped SymbolStyle")
}