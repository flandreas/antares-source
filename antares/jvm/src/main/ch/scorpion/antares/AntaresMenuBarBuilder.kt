package ch.scorpion.antares

import ch.scorpion.antares.view.GraphViewAnimationAction
import ch.scorpion.antares.view.TestAction
import ch.scorpion.antares.view.gate.GateMnemonicAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.execution.NoiseMenu
import ch.scorpion.jabbah.execution.PrintScheduleAction
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.ui.GraphFrameSwing
import ch.scorpion.jabbah.graph.ui.GraphMenuBarBuilder
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Adds [ch.scorpion.antares] related menus to the [GraphMenuBarBuilder].
 */
class AntaresMenuBarBuilder(
	frame: GraphFrameSwing,
	eventBus: EventBus
) : GraphMenuBarBuilder(frame, eventBus) {

    override fun fillMenuBar(menuBar: JMenuBar) {
        super.fillMenuBar(menuBar)
	    if (EditAuthModule.userHolder.user.isDeveloper) {
		    menuBar.add(fillDevelopmentMenu(JMenu(Translations.getString("application.menu.development"))))
	    }
    }

    override fun fillViewMenu(menu: JMenu) {
        super.fillViewMenu(menu)
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(GateMnemonicAction(eventBus))))
    }

    override fun fillExecutionMenu(menu: JMenu): JMenu {
        super.fillExecutionMenu(menu)
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(GraphViewAnimationAction(GraphViewModule.currentGraphViewAnimationType, eventBus))))
        menu.add(NoiseMenu(listOf(ExecutionModule.noNoiseGenerator, ExecutionModule.randomNoiseGenerator)))
        return menu
    }

    private fun fillDevelopmentMenu(menu: JMenu): JMenu {
	    menu.add(ActionWrapperSwing(TestAction()))
	    menu.add(ActionWrapperSwing(DebugGraphicsAction()))
	    menu.addSeparator()
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(EnableRepaintingObserverAction())))
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(RunRepaintingObserverAction())))
        menu.add(JMenuItem(ActionWrapperSwing(PreviousRepaintingObserverLogAction())))
        menu.add(JMenuItem(ActionWrapperSwing(NextRepaintingObserverLogAction())))
        menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(PrintScheduleAction((frame as GraphFrameSwing).controller.applicationContextHolder.scheduler))))

        return menu
    }
}