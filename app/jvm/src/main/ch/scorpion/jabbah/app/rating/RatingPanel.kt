package ch.scorpion.jabbah.app.rating

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import kotlinx.coroutines.runBlocking
import java.awt.*
import javax.swing.*
import javax.swing.event.HyperlinkEvent

class RatingAction : AbstractAction("application.rating.action") {
	override fun execute(event: ActionEvent) {
		RatingPanel.showAsDialog(Frame.getFrames()[0])
	}
}

class RatingPanel(
	private val service: RatingService = DummyRatingService(),
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private const val MAX_REMARK_LENGTH = 20

		fun showAsDialog(parent: Frame) {
			DialogBuilder<RatingPanel>(parent)
				.content { dialog -> RatingPanel(closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("application.rating.dialog.title"))
				.nonResizable()
				.preferredSize(Dimension(400, 500))
				.show()
		}
	}

	private var aspects: List<RatingAspect>

	private val overallRatingPanel = OverallRatingPanel()

	private val likeMostComboBox = JComboBox<String>().apply { renderer = Renderer() }

	private val likeLeastComboBox = JComboBox<String>().apply { renderer = Renderer() }

	private val remarkTextArea = JTextArea()

	private val errorText = JLabel()

	private val sendAction = SendAction()
	private val sendButton = JButton(ActionWrapperSwing(sendAction))

	private val askLater = AskLaterAction()
	private val askLaterButton = JButton(ActionWrapperSwing(askLater))

	init {
		runBlocking {
			aspects = service.retrieveAspects()
		}

		likeMostComboBox.model = DefaultComboBoxModel(aspects.map { it.positive }.toTypedArray())
			.also { it.insertElementAt(null, 0) }
		likeLeastComboBox.model = DefaultComboBoxModel(aspects.map { it.negative }.toTypedArray())
			.also { it.insertElementAt(null, 0) }
		buildUI()

		likeMostComboBox.selectedIndex = 0
		likeLeastComboBox.selectedIndex = 0
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(15, 15, 15, 15)

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		val welcomeText = JEditorPane()
		welcomeText.border = null
		welcomeText.contentType = "text/html"
		welcomeText.text = Translations.getString("application.rating.welcome.text")
		welcomeText.addHyperlinkListener {
			if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
				System.browse(it.url.toString(), Translations.getString("application.rating.action.name"))
			}
		}
		welcomeText.isEditable = false
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

		val remarksText = JLabel(Translations.getString("application.rating.remarks.text"))
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
		errorText.foreground = Color.RED
		contentPanel.add(errorText)
		contentPanel.add(Box.createVerticalStrut(30))

		contentPanel.add(Box.createVerticalGlue())

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(askLaterButton)
		buttonPanel.add(Box.createHorizontalStrut(5))
		buttonPanel.add(sendButton)

		add(contentPanel, BorderLayout.CENTER)
		add(buttonPanel, BorderLayout.SOUTH)
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
			likeMost = likeMostComboBox.selectedItem as String,
			likeLeast = likeLeastComboBox.selectedItem as String,
			remark = StringUtils.orNull(remarkTextArea.text)
		)
		runBlocking {
			service.sendRating(rating)
		}
	}

	private class Renderer : DefaultListCellRenderer() {
		override fun getListCellRendererComponent(
			list: JList<*>?,
			value: Any?,
			index: Int,
			isSelected: Boolean,
			cellHasFocus: Boolean
		): Component =
			(super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel).also {
				it.text = value?.toString() ?: Translations.getString("application.rating.chooseFromList.text")
			}
	}

	private inner class SendAction : AbstractAction("application.rating.dialog.action.send") {
		override fun execute(event: ActionEvent) {
			if (validateRatings()) {
				sendRating()
				closeHandler()
				JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					Translations.getString("application.rating.dialog.thankYou.text"),
					Translations.getString("application.rating.dialog.title"),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE)
			}
		}
	}

	private inner class AskLaterAction : AbstractAction("application.rating.dialog.action.askLater") {
		override fun execute(event: ActionEvent) {
			service.askLater()
			closeHandler()
		}
	}
}