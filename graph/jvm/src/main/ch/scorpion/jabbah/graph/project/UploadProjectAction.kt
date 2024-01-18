package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryFolderAction
import ch.scorpion.jabbah.graph.login.Session
import ch.scorpion.jabbah.graph.login.SessionEvent
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class UploadProjectAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?,
	private val service: ProjectAkrabClientServiceJvm = GraphModuleJvm.projectAkrabClientServiceJvm()
) : AbstractLibraryFolderAction(
	"project.action.upload",
	Operation.Change,
	controller
) {

	private val scope = MainScope()

	private val sessionHandler: EventHandler<SessionEvent> = { updateEnabledness() }

	init {
		controller.eventBus.register(SessionEvent::class, sessionHandler)
	}

	override fun dispose() {
		super.dispose()
		controller.eventBus.unregister(sessionHandler)
	}

	// TODO: Shouldn't this be in the base class?
	override val operationAuthorized: Boolean
		get() = operationTarget.invoke()?.let { Authorizer.isCurrentUserAuthorizedTo(Operation.Change, it) } ?: false

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness() && Session.exists

	override fun execute(event: ActionEvent) {
		if (JOptionPane.showConfirmDialog(
			SwingUtilities.getWindowAncestor(controller.view as Component),
			Translations.getString("project.action.upload.text"),
			name,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE
		) != JOptionPane.YES_OPTION) {
			return
		}

		InvocationHandler.invoke {
			scope.launch(Dispatchers.Main) {
				try {
					service.upload(selectedFolder as Project)

					JOptionPane.showConfirmDialog(
						SwingUtilities.getWindowAncestor(controller.view as Component),
						Translations.getString("project.action.upload.success.msg"),
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE)
				} catch (e: AkrabApiException) {
					val message = when (e.error.type) {
						AkrabApiError.TYPE_QUOTA -> e.error.msg!!
						else -> Translations.getString("project.action.upload.error.msg")
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