package ch.scorpion.antares

import ch.scorpion.antares.view.GraphViewAnimationAction
import ch.scorpion.antares.view.gate.AmericanSymbolStyleAction
import ch.scorpion.antares.view.gate.EuropeanSymbolStyleAction
import ch.scorpion.antares.view.gate.GateMnemonicAction
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.NoiseMenu
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.RandomNoiseGenerator
import ch.scorpion.jabbah.graph.ui.GraphMenuBarBuilder
import javax.swing.ButtonGroup
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem


/**
 * Adds [antares] related menus to the [MenuBarBuilder].
 */
class AntaresMenuBarBuilder(application: DesktopApplication, eventBus: EventBus) : GraphMenuBarBuilder(application, eventBus) {

    override fun fillViewMenu(menu: JMenu) {
        super.fillViewMenu(menu)

        val symbolStyleMenu = JMenu(Translations.getString("antares.action.symbolStyle"))

        val americanMenuItem = JRadioButtonMenuItem(AmericanSymbolStyleAction())
        val europeanMenuItem = JRadioButtonMenuItem(EuropeanSymbolStyleAction())
        val group = ButtonGroup()
        group.add(americanMenuItem)
        group.add(europeanMenuItem)

        symbolStyleMenu.add(americanMenuItem)
        symbolStyleMenu.add(europeanMenuItem)

        menu.addSeparator()
        menu.add(symbolStyleMenu)

        menu.add(JCheckBoxMenuItem(GateMnemonicAction(eventBus)))
    }

    override fun fillExecutionMenu(menu: JMenu): JMenu {
        super.fillExecutionMenu(menu)
        menu.add(JCheckBoxMenuItem(GraphViewAnimationAction(AntaresViewModule.currentGraphViewAnimationType, eventBus)))
        menu.add(NoiseMenu(listOf(ExecutionModule.noNoiseGenerator, ExecutionModule.randomNoiseGenerator)))
        return menu
    }
}