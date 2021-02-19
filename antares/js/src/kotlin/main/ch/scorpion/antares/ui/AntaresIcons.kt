package ch.scorpion.antares.ui

import ch.scorpion.jabbah.base.mreact.IconProviderRegistry
import ch.scorpion.jabbah.base.mreact.svgIcon
import ch.scorpion.jabbah.base.mreact.svgPath
import react.RBuilder
import react.ReactElement

fun RBuilder.andGate(): ReactElement =
	svgIcon(28, 28, "0 0 28 28", "none") {
		it.svgPath("M5.5 22.5V5.5H14.5C25 5.5 25 22.5 14.5 22.5H5.5Z", "black")
		it.svgPath("M22.5 14.5H27M5.5 8.5H0M5.5 19.5H0", "black")
	}

fun registerAntaresIconsInProvider() {
	IconProviderRegistry.register("/img/and.png") { RBuilder().andGate() }
}
