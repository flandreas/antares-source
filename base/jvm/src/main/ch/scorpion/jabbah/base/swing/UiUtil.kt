package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ui.UI
import java.awt.Color
import java.awt.EventQueue
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.image.BaseMultiResolutionImage
import java.lang.reflect.InvocationTargetException
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.plaf.FontUIResource
import javax.swing.text.JTextComponent
import javax.swing.text.html.HTMLEditorKit
import kotlin.math.max
import kotlin.math.min


/**
 * Contains various static utility methods.
 */
object UiUtil {

	private val LOG by lazy { logger(UiUtil::class) }

	private const val DIVERT = 24
	private val VARIANT_QUALIFIERS = listOf("", "@125pct", "@150pct", "@2x")

	/**
	 * Error text color is used for drawing text directly on the standard panel background,
	 * such as validation error text in a dialog.
	 */
	private val ERROR_TEXT_COLOR_DARK = Color(255, 107, 104)
	private val ERROR_TEXT_COLOR_LIGHT = Color.RED

	/**
	 * Success and failure colors are used for drawing areas in the UI that represent success (red)
	 * or failure (green), such as backgrounds of table cells.
	 */
	private val SUCCESS_BACKGROUND_COLOR_DARK =  Color(7, 87, 0)
	private val SUCCESS_BACKGROUND_COLOR_LIGHT = Color(232, 255, 232)
	private val FAILURE_BACKGROUND_COLOR_DARK = Color(116, 55, 56)
	private val FAILURE_BACKGROUND_COLOR_LIGHT = Color(255, 232, 232    )

	val errorTextColor: Color get() = if (UI.isDark) {
		ERROR_TEXT_COLOR_DARK
	} else {
		ERROR_TEXT_COLOR_LIGHT
	}

	val successBackgroundColor: Color get() = if (UI.isDark) {
		SUCCESS_BACKGROUND_COLOR_DARK
	} else {
		SUCCESS_BACKGROUND_COLOR_LIGHT
	}
	val successTextColor: Color get() = UIManager.getColor("Label.foreground")

	val failureBackgroundColor: Color get() = if (UI.isDark) {
		FAILURE_BACKGROUND_COLOR_DARK
	} else {
		FAILURE_BACKGROUND_COLOR_LIGHT
	}
	val failureTextColor: Color get() = UIManager.getColor("Label.foreground")

	/**
	 * Invokes the specified invokable on the [EventQueue] an returns immediately.
	 * The lambda returns `true` if it has dispatched the invokable on the event dispatch thread,
	 * `false` if we are already on the event dispatch thread, and this lambda did nothing.
	 *
	 * Can be replaced for testing purposes with an implementations that does nothing and always
	 * return `false`.
	 */
	var eventQueueInvoker: (invokable: () -> Unit) -> Boolean = {
		if (!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater { it.invoke() }
		}
		false
	}

    /**
     * A wrapper around [SwingUtilities.invokeAndWait] which wraps exceptions in
     * [RuntimeException]s. If called from the EDT, the runnable is simply run.
     */
    fun invokeAndWait(runnable: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run()
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable)
            } catch (e: InterruptedException) {
                // swallow
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e.cause)
            }

        }
    }

    /**
     * A wrapper around [SwingUtilities.invokeAndWait].
     */
    @Throws(InterruptedException::class, InvocationTargetException::class)
    fun invokeAndWaitThrowing(runnable: Runnable) {
        SwingUtilities.invokeAndWait(runnable)
    }

    /**
     * A wrapper around [SwingUtilities.invokeAndWait].
     */
    fun invokeLater(runnable: Runnable) {
        SwingUtilities.invokeLater(runnable)
    }

    fun setUIFont(f: FontUIResource) {
        val keys = UIManager.getDefaults().keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val value = UIManager.get(key)
            if (value != null && value is FontUIResource) {
	            UIManager.put(key, f)
            }
        }
    }

	fun decorateTextArea(textArea: JTextArea): JScrollPane {
		val scroll = JScrollPane(textArea)
		scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		val border = UIManager.getBorder("TextField.border")
		if (border != null) {
			scroll.border = border
		}
		return scroll
	}

	fun getBackgroundDivertColor(parent: JComponent): Color =
		getBackgroundDivertColor(parent.background)

	fun getBackgroundDivertColor(bg: Color): Color =
		if (isDark(bg)) {
			Color(min(255, bg.red + DIVERT), min(255, bg.green + DIVERT), min(255, bg.blue + DIVERT))
		} else {
			Color(max(0, bg.red - DIVERT), max(0, bg.green - DIVERT), max(0, bg.blue - DIVERT))
		}

	fun getButtonPressColor(parent: JComponent): Color {
		val bg = parent.background
		return Color(bg.red - 40, bg.green - 40, bg.blue - 40)
	}

	fun createToolBarButton(action: Action, toggle: Boolean = false): AbstractButton {
		val button = createToolBarButton(ActionWrapperSwing(action), toggle)
		action.imagePath?.let { button.icon = themedIcon(it) }
		button.toolTipText = action.name
		return button
	}

	fun createToolBarButton(swingAction: javax.swing.Action, toggle: Boolean): AbstractButton {
		val button = if (toggle) {
			JToggleButton(swingAction)
		} else {
			JButton(swingAction)
		}
		button.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
		button.text = null
		RolloverButtonEnabler(button)
		return button
	}

	fun darkImagePath(path: String): String = path.replace(".", "-dark.")

	/**
	 * Creates an [ImageIcon] from the specified base path, depending on whether the current UI
	 * has a dark or a light theme.
	 *
	 * For dark themes, the image file base name is expanded by `-dark`.
	 * For example, a file name `example.png` is expanded to `example-dark.png` for dark themes.
	 * If no dark file version is found, the normal version is used (without `-dark` expansion).
	 *
	 * Also tries to load multiple resolution variants whose file names contain a resolution variant
	 * like `@2x`, e.g. `example@2x.png` or `example-dark@2x.png`.
	 */
	fun themedIcon(path: String, clazz: Class<*> = UiUtil::class.java): ImageIcon {
		return if (UI.isDark) {
			try {
				getMultiResolutionIcon(path, ::getDarkVariantPath, clazz)
			} catch (e: IllegalArgumentException) {
				getMultiResolutionIcon(path, ::getVariantPath, clazz)
			}
		} else {
			getMultiResolutionIcon(path, ::getVariantPath, clazz)
		}
	}

	private val focusGainedHandler = object : FocusAdapter() {
		override fun focusGained(e: FocusEvent?) {
			if (e?.source is JTextComponent) {
				(e.source as JTextComponent).selectAll()
			}
		}
	}

	/** Configures a [JTextComponent] to select all text when it receives the focus.*/
	fun selectAllOnFocusGained(c: JTextComponent) {
		c.addFocusListener(focusGainedHandler)
	}

	fun createHtmlEditorPane(text: String, actionNameKey: String?): JEditorPane {
		val editorPane = JEditorPane()

		if (UI.isDark) {
			val htmlKit = HTMLEditorKit()
			htmlKit.styleSheet.addRule("a { color: rgb(40,123,222); }")
			editorPane.editorKit = htmlKit
		}

		editorPane.contentType = "text/html"
		editorPane.isEditable = false
		editorPane.border = null
		editorPane.text = text

		if (actionNameKey != null) {
			editorPane.addHyperlinkListener {
				if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
					try {
						System.browse(it.url.toString(), Translations.getString(actionNameKey))
					} catch (e: Throwable) {
						JOptionPane.showMessageDialog(
							editorPane,
							Translations.getString("base.browse.error.msg", e.message ?: "?"),
							Translations.getString(actionNameKey),
							JOptionPane.ERROR_MESSAGE
						)
					}
				}
			}
		}

		return editorPane
	}

	private fun isDark(color: Color): Boolean =
		(color.red + color.green + color.blue) / 3 < 128

	private fun getVariantPath(path: String, variant: String): String = path.replace(".", "$variant.")

	private fun getDarkVariantPath(path: String, variant: String): String = path.replace(".", "-dark$variant.")

	private fun getMultiResolutionIcon(
		path: String,
		pathResolver: (path: String, variant: String) -> String,
		clazz: Class<*>
	): ImageIcon {
		val variants = mutableListOf<ImageIcon>()
		VARIANT_QUALIFIERS.forEach { variant ->
			try {
				variants.add(ImageIcon(clazz.getResource(pathResolver(path, variant))))
			} catch (t: Throwable) {}
		}
		return when(variants.size) {
			0 -> throw IllegalArgumentException("Icon '$path' not found")
			1 -> variants[0]
			else -> ImageIcon(BaseMultiResolutionImage(*(variants.map { it.image }).toTypedArray()))
		}
	}
}