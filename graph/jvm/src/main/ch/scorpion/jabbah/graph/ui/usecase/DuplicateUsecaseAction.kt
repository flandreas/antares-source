package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.app.UsecaseAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.Frame
import javax.swing.JOptionPane

class DuplicateUsecaseAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction(controller, "usecases.action.duplicate", service, eventBus) {

	override fun execute(event: ActionEvent) {
		val newUsecaseName = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("usecase.action.duplicate.nameQuestion"),
			name,
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			Translations.getString("usecase.action.duplicate.newName", usecase!!.name.value)
		) as String?

		if (StringUtils.isEmpty(newUsecaseName)) {
			return
		}

		service.duplicateUsecase(applicationDataHolder, usecase!!.id, newUsecaseName!!)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null
}