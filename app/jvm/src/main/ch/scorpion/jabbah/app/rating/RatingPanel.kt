package ch.scorpion.jabbah.app.rating

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.action.AbstractApplicationAction
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import kotlinx.coroutines.runBlocking
import java.awt.*
import javax.swing.*
import javax.swing.event.HyperlinkEvent

class RatingAction(
	application: Application,
) : AbstractApplicationAction("application.rating.action", application) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			RatingPanel.showAsDialog(name, application, cancelable = true, Frame.getFrames()[0])
		}
	}
}

class RatingPanel(
	private val application: Application,
	private val cancelable: Boolean,
	private val service: RatingService = AppModuleJvm.ratingService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private const val MAX_REMARK_LENGTH = 200

		fun showAsDialog(title: String, application: Application, cancelable: Boolean, parent: Frame, service: RatingService = AppModuleJvm.ratingService) {
			// If this was in an InvocationHandler, dialog would never show up when
			// called from Application.handleShutDown()

			var aspects: List<RatingAspect>? = null
			var aspectError: Throwable? = null

			/**
			 * If this method is automatically called when the application finishes (cancelable = false),
			 * and there is no internet connection (which is often the case in a lab environment),
			 * we don't want to show the [RatingPanel] overlaid by an error dialog. Instead, just skip everything
			 * and don't ask for a rating.
			 *
			 * Finding out whether there is a working internet connection or not is done by pre-fetching
			 * [RatingAspect]s. If they could be successfully loaded, they are provided to the dialog in order
			 * to avoid being loaded again.
			 */
			if (!cancelable) {
				runBlocking {
					try {
						aspects = service.retrieveAspects()
					} catch (e: Throwable) {
						aspectError = e
					}
				}
			}

			if (aspectError != null) {
				return
			}

			DialogBuilder<RatingPanel>(parent)
				.content { dialog -> RatingPanel(application, cancelable, service, closeHandler = { dialog.dispose() }) }
				.title(title)
				.nonResizable()
				.preventWindowClose(!cancelable)
				.preferredSize(Dimension(400, 500))
				.onWindowOpened {
					it.loadData(aspects)
				}
				.show()
		}
	}

	private lateinit var aspects: List<RatingAspect>

	private val overallRatingPanel = OverallRatingPanel()

	private val likeMostComboBox = JComboBox<RatingAspect?>().apply { renderer = Renderer(positive = true) }

	private val likeLeastComboBox = JComboBox<RatingAspect?>().apply { renderer = Renderer(positive = false) }

	private val remarkTextArea = JTextArea()

	private val errorText = JLabel()

	private val sendAction = SendAction()
	private val sendButton = JButton(ActionWrapperSwing(sendAction))

	private val cancelAction = CancelAction()
	private val cancelButton = JButton(ActionWrapperSwing(cancelAction))

	private val askLater = AskLaterAction()
	private val askLaterButton = JButton(ActionWrapperSwing(askLater))

	init {
		buildUI()
		isEnabled = false
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		val welcomeText = UiUtil.createHtmlEditorPane(
			Translations.getString("application.rating.welcome.text", application.displayName),
			"application.rating.action.name")
		welcomeText.alignmentX = Component.LEFT_ALIGNMENT
		welcomeText.maximumSize = welcomeText.preferredSize
		contentPanel.add(welcomeText)
		contentPanel.add(Box.createVerticalStrut(20))

		overallRatingPanel.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(overallRatingPanel)
		contentPanel.add(Box.createVerticalGlue())
		contentPanel.add(Box.createVerticalStrut(30))

		val likeMostText = JLabel(Translations.getString("application.rating.likedMost.text"))
		likeMostText.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(likeMostText)
		contentPanel.add(Box.createVerticalStrut(4))
		likeMostComboBox.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(likeMostComboBox)
		contentPanel.add(Box.createVerticalStrut(20))

		val likeLeastText = JLabel(Translations.getString("application.rating.likedLeast.text"))
		likeLeastText.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(likeLeastText)
		contentPanel.add(Box.createVerticalStrut(4))
		likeLeastComboBox.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(likeLeastComboBox)
		contentPanel.add(Box.createVerticalStrut(20))

		val remarksText = JLabel(Translations.getString("application.rating.remarks.text", MAX_REMARK_LENGTH))
		remarkTextArea.rows = 6
		remarkTextArea.columns = 30
		val remarksScrollPane = JScrollPane(remarkTextArea)
		remarksScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		remarksScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		remarksScrollPane.minimumSize = remarksScrollPane.preferredSize
		remarksText.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(remarksText)
		contentPanel.add(Box.createVerticalStrut(4))
		remarksScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(remarksScrollPane)
		contentPanel.add(Box.createVerticalStrut(20))

		errorText.text = " "
		errorText.foreground = UiUtil.errorTextColor
		contentPanel.add(errorText)
		contentPanel.add(Box.createVerticalStrut(30))

		contentPanel.add(Box.createVerticalGlue())

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(askLaterButton)
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		if (cancelable) {
			buttonPanel.add(cancelButton)
			buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		}
		buttonPanel.add(sendButton)

		add(contentPanel, BorderLayout.CENTER)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun loadData(aspects: List<RatingAspect>?) {
		InvocationHandler.invoke {
			runBlocking {
				try {
					if (aspects == null) {
						setAspects(service.retrieveAspects())
					} else {
						setAspects(aspects)
					}
				} catch (e: Throwable) {
					showLoadError()
					closeHandler()
					return@runBlocking
				}
			}
		}
	}

	private fun setAspects(aspects: List<RatingAspect>) {
		this.aspects = aspects

		likeMostComboBox.model = DefaultComboBoxModel(aspects.toTypedArray())
			.also { it.insertElementAt(null, 0) }
		likeLeastComboBox.model = DefaultComboBoxModel(aspects.toTypedArray())
			.also { it.insertElementAt(null, 0) }


		likeMostComboBox.selectedIndex = 0
		likeLeastComboBox.selectedIndex = 0

		isEnabled = true
	}

	private fun showLoadError() {
		JOptionPane.showConfirmDialog(
			this@RatingPanel,
			Translations.getString("application.rating.loadError.text"),
			Translations.getString("application.rating.dialog.title"),
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun validateRatings(): Boolean {
		if (overallRatingPanel.rating < 1) {
			errorText.text = Translations.getString("application.rating.validation.overall.text")
			return false
		}
		if (likeMostComboBox.selectedIndex < 1) {
			errorText.text = Translations.getString("application.rating.validation.likedMost.text")
			return false
		}
		if (likeLeastComboBox.selectedIndex < 1) {
			errorText.text = Translations.getString("application.rating.validation.likedLeast.text")
			return false
		}
		if (remarkTextArea.text.length > MAX_REMARK_LENGTH) {
			errorText.text = Translations.getString("application.rating.validation.remarkLength.text", MAX_REMARK_LENGTH)
			return false
		}
		return true
	}

	private fun sendRating() {
		val rating = Rating(
			overallRating = overallRatingPanel.rating,
			likeMost = likeMostComboBox.selectedItem as RatingAspect,
			likeLeast = likeLeastComboBox.selectedItem as RatingAspect,
			remark = StringUtils.orNull(remarkTextArea.text)
		)
		runBlocking {
			if (!service.sendRating(rating)) {
				showSendError()
			}
		}
	}

	private fun showSendError() {
		JOptionPane.showConfirmDialog(
			this@RatingPanel,
			Translations.getString("application.rating.sendError.text"),
			Translations.getString("application.rating.dialog.action.send.name"),
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private class Renderer(val positive: Boolean) : DefaultListCellRenderer() {
		override fun getListCellRendererComponent(
			list: JList<*>?,
			value: Any?,
			index: Int,
			isSelected: Boolean,
			cellHasFocus: Boolean
		): Component =
			(super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel).also {
				it.text = if (value == null) {
					Translations.getString("application.rating.chooseFromList.text")
				} else if (positive) {
					(value as RatingAspect).positive
				} else {
					(value as RatingAspect).negative
				}
			}
	}

	private inner class SendAction : AbstractAction("application.rating.dialog.action.send") {
		override fun execute(event: ActionEvent) {
			if (validateRatings()) {
				InvocationHandler.invoke {
					sendRating()
					closeHandler()
					showThankYou()
				}
			}
		}

		private fun showThankYou() {
			JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("application.rating.dialog.thankYou.text"),
				Translations.getString("application.rating.dialog.title"),
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE)
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}

	private inner class AskLaterAction : AbstractAction("application.rating.dialog.action.askLater") {
		override fun execute(event: ActionEvent) {
			service.askLater()
			closeHandler()
		}
	}
}