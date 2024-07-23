package ch.scorpion.antares

import ch.scorpion.antares.hdl.vhdl.ExportVHDLAction
import ch.scorpion.antares.view.GraphViewAnimationAction
import ch.scorpion.antares.view.TestAction
import ch.scorpion.antares.view.analysis.AnalyseCircuitAction
import ch.scorpion.antares.view.expression.NewBooleanExpressionAction
import ch.scorpion.antares.view.gate.GateMnemonicAction
import ch.scorpion.antares.view.net.tunnel.GlobalTunnelAction
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTableAction
import ch.scorpion.antares.view.truthtable.NewTruthTableAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.view.DummyViewSpaceReductionAction
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

	companion object {
		fun createSynthesisMenu() = JMenu(Translations.getString("application.menu.synthesis"))
	}

    override fun fillMenuBar(menuBar: JMenuBar) {
        super.fillMenuBar(menuBar)
	    menuBar.add(fillSynthesisMenu(createSynthesisMenu()))
	    if (EditAuthModule.userHolder.user.isDeveloper) {
		    menuBar.add(fillDevelopmentMenu(JMenu(Translations.getString("application.menu.development"))))
	    }
    }

	override fun fillFileMenu(menu: JMenu) {
		super.fillFileMenu(menu)
		menu.addSeparator()
		menu.add(ActionWrapperSwing(GlobalTunnelAction()))
	}

	override fun fillExportMenu(menu: JMenu) {
		super.fillExportMenu(menu)
		menu.add(JMenuItem(ActionWrapperSwing(ExportVHDLAction())))
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
	    menu.add(ActionWrapperSwing(TestAction(graphFrame.editor)))
	    menu.add(ActionWrapperSwing(DebugGraphicsAction()))
	    menu.add(JCheckBoxMenuItem(ActionWrapperSwing(DummyViewSpaceReductionAction())))
	    menu.addSeparator()
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(EnableRepaintingObserverAction())))
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(RunRepaintingObserverAction())))
        menu.add(JMenuItem(ActionWrapperSwing(PreviousRepaintingObserverLogAction())))
        menu.add(JMenuItem(ActionWrapperSwing(NextRepaintingObserverLogAction())))
        menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(PrintScheduleAction((frame as GraphFrameSwing).controller.applicationContextHolder.scheduler))))

        return menu
    }

	private fun fillSynthesisMenu(menu: JMenu): JMenu {
		with (graphFrame.controller.graphPanelViewController.libraryPanelController.libraryTreeViewController) {
			menu.add(JMenuItem(ActionWrapperSwing(NewTruthTableAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(CreateCircuitFromTruthTableAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(NewBooleanExpressionAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(AnalyseCircuitAction(this))))
		}
		return menu
	}
}