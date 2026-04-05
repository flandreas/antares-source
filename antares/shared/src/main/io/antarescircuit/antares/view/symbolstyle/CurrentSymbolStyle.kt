package io.antarescircuit.antares.view.symbolstyle

import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule

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