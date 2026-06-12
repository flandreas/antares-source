package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractLibraryDirectoryAction
import io.antarescircuit.jabbah.graph.library.LibraryVisibility
import io.antarescircuit.jabbah.graph.login.Session
import io.antarescircuit.jabbah.graph.login.SessionEvent
import io.antarescircuit.jabbah.graph.module.GraphModuleJvm
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class UploadProjectAction(
	controller: LibraryTreeViewController,
	private val service: ProjectAkrabClientService = GraphModuleJvm.projectAkrabClientService()
) : AbstractLibraryDirectoryAction(
	"project.action.upload",
	Operation.Change,
	controller
) {

	private val scope = MainScope()

	private val sessionHandler: EventHandler<SessionEvent> = { updateEnabled() }

	init {
		controller.eventBus.register(SessionEvent::class, sessionHandler)
	}

	override fun dispose() {
		super.dispose()
		controller.eventBus.unregister(sessionHandler)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && Session.exists

	override fun execute(event: ActionEvent) {
		val project = selectedFolder as Project

		if (project.expandedImports.hasCustomLibrary) {
			JOptionPane.showConfirmDialog(
				SwingUtilities.getWindowAncestor(controller.view as Component),
				Translations.getString("project.action.upload.customLib.txt"),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE
			)
			return
		}

		if (JOptionPane.showConfirmDialog(
			SwingUtilities.getWindowAncestor(controller.view as Component),
			Translations.getString("project.action.upload.text"),
			name,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE
		) != JOptionPane.YES_OPTION) {
			return
		}

		if (project.visibility == LibraryVisibility.Private) {
			if (JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(controller.view as Component),
					Translations.getString("project.action.upload.privateWarning.text"),
					name,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				) != JOptionPane.YES_OPTION) {
				return
			}
		}

		InvocationHandler.invoke {
			scope.launch(Dispatchers.Main) {
				try {
					service.upload(project)

					JOptionPane.showConfirmDialog(
						SwingUtilities.getWindowAncestor(controller.view as Component),
						Translations.getString("project.action.upload.success.msg"),
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE)
				} catch (e: AkrabApiException) {
					val message = when (e.error.type) {
						AkrabApiError.TYPE_QUOTA -> e.error.msg!!
						else -> Translations.getString("project.action.upload.error.msg", e.error.msg ?: "")
					}
					JOptionPane.showConfirmDialog(
						SwingUtilities.getWindowAncestor(controller.view as Component),
						message,
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE)
				} catch (e: Exception) {
					JOptionPane.showConfirmDialog(
						SwingUtilities.getWindowAncestor(controller.view as Component),
						Translations.getString("project.action.upload.netError.msg", e.message ?: ""),
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE)
				}
			}
		}
	}
}