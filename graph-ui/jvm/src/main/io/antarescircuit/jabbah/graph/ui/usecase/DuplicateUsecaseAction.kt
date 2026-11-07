package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.view.app.UsecaseAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import java.awt.Frame
import javax.swing.JOptionPane

class DuplicateUsecaseAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction(controller, "usecases.action.duplicate", service, eventBus) {

	override val opensDialog: Boolean get() = true

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