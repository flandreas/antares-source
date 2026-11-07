package io.antarescircuit.antares

import io.antarescircuit.antares.ai.ShowAiChatAction
import io.antarescircuit.antares.hdl.vhdl.ExportVHDLAction
import io.antarescircuit.antares.view.GraphViewAnimationAction
import io.antarescircuit.antares.view.TestAction
import io.antarescircuit.antares.view.analysis.AnalyseCircuitAction
import io.antarescircuit.antares.view.expression.NewBooleanExpressionAction
import io.antarescircuit.antares.view.fsm.NewFSMAction
import io.antarescircuit.antares.view.gate.GateMnemonicAction
import io.antarescircuit.antares.view.net.tunnel.GlobalTunnelAction
import io.antarescircuit.antares.view.synthesis.CreateCircuitFromTruthTableAction
import io.antarescircuit.antares.view.truthtable.NewTruthTableAction
import io.antarescircuit.jabbah.app.dump.SystemMalfunctionPanel
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.draw.view.*
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.view.DummyViewSpaceReductionAction
import io.antarescircuit.jabbah.execution.NoiseMenu
import io.antarescircuit.jabbah.execution.PrintScheduleAction
import io.antarescircuit.jabbah.execution.module.ExecutionModule
import io.antarescircuit.jabbah.graph.ui.GraphFrameSwing
import io.antarescircuit.jabbah.graph.ui.GraphMenuBarBuilder
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Adds [io.antarescircuit.antares] related menus to the [GraphMenuBarBuilder].
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
	    menu.add(ActionWrapperSwing(TestAction(graphFrame.application, graphFrame.editor)))
	    menu.add(ActionWrapperSwing(DebugGraphicsAction()))
	    menu.add(JCheckBoxMenuItem(ActionWrapperSwing(DummyViewSpaceReductionAction())))
        menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(PrintScheduleAction((frame as GraphFrameSwing).controller.applicationContextHolder.scheduler))))
		menu.add(JMenuItem(ActionWrapperSwing(SystemMalfunctionPanel.createDeveloperAction(frame.application))))

        return menu
    }

	private fun fillSynthesisMenu(menu: JMenu): JMenu {
		with (graphFrame.controller.graphPanelViewController.libraryPanelController.libraryTreeViewController) {
			menu.add(JMenuItem(ActionWrapperSwing(NewTruthTableAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(CreateCircuitFromTruthTableAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(NewBooleanExpressionAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(AnalyseCircuitAction(this))))
			menu.add(JMenuItem(ActionWrapperSwing(NewFSMAction(this))))
		}
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(
			ShowAiChatAction(panelProvider = { (graphFrame as? AntaresFrameSwing)?.aiChatPanel }))))
		return menu
	}
}
