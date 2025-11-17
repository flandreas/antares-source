package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.SnappableX
import ch.scorpion.jabbah.edit.SnappableXCoordinate
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm.Companion.TEXT_PAINTER
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextPane
import javax.swing.border.LineBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/** Implements [TextComponentFactory] on the JVM platform. */
class TextComponentFactoryJvm : TextComponentFactory {

	override fun create(text: TranslatableText, location: Point2D, styleType: StyleType, styleProvider: StyleProvider): TextComponent {
		return TextComponentJvm(text, location, styleType, styleProvider)
	}
}

/**
 * A [Component] that supports inline text editing using JDK classes.
 */
open class TextComponentJvm(
	text: TranslatableText = TranslatableText(""),
	location: Point2D = Point2D.ZERO,
	styleType: StyleType = StyleType.TEXT,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractTextComponent(
	styleType = styleType,
	styleProvider = styleProvider,
	shape = Rectangle2D(location.x, location.y, 0.0, 0.0)
), Transparent, TextComponent {

	private companion object {

		private val LOG by logger(TextComponentJvm::class)

		/**
		 * A shared instance of the [JTextPane][javax.swing.JTextPane] that is used for painting
		 * [TextComponent]s. This shared instance is kept for performance reasons.
		 */
		private val TEXT_PAINTER = JTextPane()

		/**
		 * A shared instance of the [JTextPane][javax.swing.JTextPane] that is used for editing text. This shared
		 * instance is kept for performance reasons.
		 */
		private val TEXT_EDITOR = JTextPane()

		/**
		 * A shared instance of the [JTextPane][javax.swing.JTextPane] that is used for measuring the preferred size of
		 * [TextComponent]s. This shared instance is kept for performance reasons.
		 */
		private val TEXT_MEASURER = JTextPane()

		init {
			// install the shared text painter object
			TEXT_PAINTER.isOpaque = false
			TEXT_PAINTER.border = javax.swing.border.EmptyBorder(1, 1, 1, 1)

			// install the shared text editor object
			TEXT_EDITOR.isOpaque = true
			TEXT_EDITOR.border = TextFieldBorder()
			TEXT_EDITOR.isDoubleBuffered = false

			// install the shared text measurer object
			TEXT_MEASURER.isOpaque = true
			TEXT_MEASURER.border = TextFieldBorder()
			TEXT_MEASURER.isDoubleBuffered = false

			// Explicitly set an arbitrary size to avoid that getPreferredSize()
			// returns a minimum size. Otherwise, the empty text editor would be sized
			// to a height of zero pixels. This is needed for JDK 1.3, but apparently not
			// for JDK 1.4 any more. See BasicTextUI.getPreferredSize().
			TEXT_MEASURER.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE)
		}

		/**
		 * Utility function that measures the size of a certain text as it would be rendered with the specified font and
		 * zoom factor. In fact, the measured size is the preferred size of a [JTextPane] that has
		 * been setup with the specified configuration.
		 *
		 * @param text the text whose size is to be measured
		 * @param font the font for which the text is to be measured
		 * @param zoomFactor the zoom factor that will be used when rendering the text
		 * @return the preferred size of the [JTextPane] that would render the text using the specified configuration
		 */
		private fun measureText(text: String, font: java.awt.Font, zoomFactor: Double): Dimension2D {
			val a = SimpleAttributeSet()

			StyleConstants.setFontFamily(a, font.family)
			StyleConstants.setFontSize(a, (font.size2D * zoomFactor).toInt())
			StyleConstants.setBold(a, font.isBold)
			StyleConstants.setItalic(a, font.isItalic)

			TEXT_MEASURER.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE)
			TEXT_MEASURER.text = text
			TEXT_MEASURER.selectAll()
			TEXT_MEASURER.setParagraphAttributes(a, true)

			return Dimension2D(
				TEXT_MEASURER.preferredSize.width,
				TEXT_MEASURER.preferredSize.height)
		}
	}

	override var text: Translatable = text
		set(value) {
			if (value != field) {
				if (value.isEmpty) {
					throw IllegalArgumentException(Translations.getString("edit.property.text.empty.error"))
				}
				invalidate()
				field = value
				invalidate()
				update()
				validate()
			}
		}

	private val eventHandler = EventHandler()

	init {
		filled = false
		stroked = false
		adjustBounds()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("hAlign", horizontalAlignment.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("hAlign")) {
			horizontalAlignment = HorizontalAlignment.withName(reader.readString("hAlign"))
		}
	}

	/** ---- [SnappableX] */

	override val snappableX: Array<SnappableX>
		get() = arrayOf(
			when(horizontalAlignment) {
				HorizontalAlignment.LEFT -> SnappableXCoordinate(minX)
				HorizontalAlignment.CENTER -> SnappableXCoordinate(centerX)
				HorizontalAlignment.RIGHT -> SnappableXCoordinate(maxX)
			}
		)

	/** ---- [Drawable] */

	override fun draw(context: DrawContext) {
		if (filled) {
			decorator.drawBackground(this, context)
		}

		val oldClip = context.g.getClipBounds()
		context.g.clip(x.toInt(), y.toInt(), width.toInt(), height.toInt())

		setupTextPainter(context, filled)

		context.translated(TEXT_PAINTER.x.toDouble(), TEXT_PAINTER.y.toDouble()) {
			TEXT_PAINTER.paint((it.g as Graphics2DJvm).g)
		}

		context.g.setClipBounds(oldClip)

		if (DrawModule.debugGfx) {
			DrawModule.drawLocatableDebugBoundingBox(this, context)
		}

		if (stroked) {
			decorator.drawForeground(this, context)
		}
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		if (context is EditInputEventContext) {
			@Suppress("UNCHECKED_CAST")
			eventHandler as InputEventHandler<T>
		} else {
			InputEventHandlerAdapter.EMPTY_HANDLER
		}

	/** ---- [TextComponent] */

	/**
	 * Adjusts the size of this [TextComponent]'s shape to the preferred size of the current text, expanded by
	 * the constant horizontal and vertical insets.
	 *
	 * Currently, this method only changes the width and height of the shape, but not its position. It should be
	 * considered whether this method should also change the position of the shape, depending on the current
	 * alignment settings.
	 */
	private fun adjustBounds() {
		// TODO change the position of the bounding box depending on alignment?
		// Note that TextEditTool uses its own adjustment strategy for inline editing,
		// which does change the position depending on alignment
		val b = shape
		val dim = measureText(text.getTranslation(), Graphics2DJvm.toAwtFont(font), 1.0)
		setFrame(b.x, b.y, dim.width + 2 * INSET_X, dim.height + 2 * INSET_Y)
	}

	/** Adjust position and size of the currently used text editor. */
	private fun adjustTextEditor(editor: Editor) {
		val bounds = shape
		LOG.trace("Adjust text editor at $bounds")
		val orig = editor.view.modelToView(Point2D(bounds.x + INSET_X, bounds.y + INSET_Y))
		TEXT_EDITOR.setBounds(
			orig.x.toInt(),
			orig.y.toInt(),
			((bounds.width - 2 * INSET_X) * editor.view.zoomFactor).toInt(),
			TEXT_EDITOR.preferredSize.height)
	}

	/**
	 * Sets up the shared instance of the [TEXT_PAINTER] object with everything it needs to properly render the text.
	 */
	private fun setupTextPainter(context: DrawContext, filled: Boolean) {
		val attr = SimpleAttributeSet()
		val awtFont = Graphics2DJvm.toAwtFont(font)

		StyleConstants.setFontFamily(attr, awtFont.family)
		StyleConstants.setFontSize(attr, awtFont.size)
		StyleConstants.setBold(attr, awtFont.isBold)
		StyleConstants.setItalic(attr, awtFont.isItalic)
		StyleConstants.setAlignment(attr, horizontalAlignmentSwing)

		if (filled) {
			StyleConstants.setForeground(
				attr, Graphics2DJvm.toAwtColor(
					if (context.useContextColors) context.color!!.textColor else transparent.applyTo(color.textColor)
				)
			)
			StyleConstants.setBackground(
				attr, Graphics2DJvm.toAwtColor(
					if (context.useContextColors) context.color!!.backgroundColor else transparent.applyTo(color.backgroundColor)
				)
			)
		} else {
			StyleConstants.setForeground(
				attr, Graphics2DJvm.toAwtColor(
					if (context.useContextColors) context.color!!.textColor else transparent.applyTo(color.foregroundColor)
				)
			)
			StyleConstants.setBackground(
				attr, Graphics2DJvm.toAwtColor(
					if (context.useContextColors) context.color!!.backgroundColor else transparent.applyTo(color.textColor)
				)
			)
		}

		val bounds = shape
		TEXT_PAINTER.setBounds(
			bounds.x.toInt() + INSET_X,
			bounds.y.toInt() + INSET_Y,
			bounds.width.toInt() - 2 * INSET_X,
			Integer.MAX_VALUE)
		TEXT_PAINTER.text = text.getTranslation()
		TEXT_PAINTER.selectAll()
		TEXT_PAINTER.setParagraphAttributes(attr, true)
	}

	private val horizontalAlignmentSwing: Int get() =
		when(horizontalAlignment) {
			HorizontalAlignment.LEFT -> StyleConstants.ALIGN_LEFT
			HorizontalAlignment.CENTER -> StyleConstants.ALIGN_CENTER
			HorizontalAlignment.RIGHT -> StyleConstants.ALIGN_RIGHT
		}

	private fun setupTextEditor(zoomFactor: Double) {
		val attr = SimpleAttributeSet()
		val awtFont = Graphics2DJvm.toAwtFont(font)

		StyleConstants.setFontFamily(attr, awtFont.family)
		StyleConstants.setFontSize(attr, (awtFont.size * zoomFactor).toInt())
		StyleConstants.setBold(attr, awtFont.isBold)
		StyleConstants.setItalic(attr, awtFont.isItalic)
		StyleConstants.setForeground(attr, Graphics2DJvm.toAwtColor(color.textColor))
		StyleConstants.setAlignment(attr, horizontalAlignmentSwing)

		TEXT_EDITOR.text = ""
		TEXT_EDITOR.setParagraphAttributes(attr, true)
		TEXT_EDITOR.text = text.getTranslation()
	}

	/**
	 * [TextFieldBorder] draws an orange, dashed lines around the text editor and provides a graphical feedback
	 * that the editor is currently active.
	 */
	private class TextFieldBorder : LineBorder(java.awt.Color.ORANGE, 1) {
		companion object {
			val STROKE = BasicStroke(0.1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, floatArrayOf(5f, 5f), 0f)
		}

		override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
			(g as Graphics2D).stroke = STROKE
			super.paintBorder(c, g, x, y, width, height)
		}
	}

	private inner class EventHandler : InputEventHandlerAdapter<EditInputEventContext>(), DocumentListener, FocusListener {

		private var editor: Editor? = null
		private var editing: Boolean = false

		/** ---- [InputEventHandler] interface */

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			return if (editing) this else null
		}

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (editing) {
				stopEditing()
			}
			return null
		}

		override fun mouseClicked(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (context.mouseEvent?.clickCount == 2) {
				startEditing(context.editor)
				return this
			}
			return null
		}

		override fun focusGained(e: FocusEvent?) {}

		override fun focusLost(e: FocusEvent?) {
			stopEditing()
		}

		/** ---- [DocumentListener] */

		override fun changedUpdate(e: DocumentEvent?) {
			requestLayoutTextEditor()
		}

		override fun insertUpdate(e: DocumentEvent?) {
			requestLayoutTextEditor()
		}

		override fun removeUpdate(e: DocumentEvent?) {
			requestLayoutTextEditor()
		}

		/** ---- [EventHandler] */

		private fun startEditing(editor: Editor) {
			LOG.trace("start editing")
			this.editor = editor
			this.editor!!.commandManager.active = false
			editing = true

			setupTextEditor(editor.view.zoomFactor)
			adjustTextEditor(editor)

			(editor.view.canvas as Container).add(TEXT_EDITOR)
			(editor.view.canvas as Container).revalidate()
			(editor.view.canvas as Container).repaint()

			// request focus and position caret within the text
			TEXT_EDITOR.requestFocusInWindow()

			// listen for text changes to adjust the editor's size
			TEXT_EDITOR.document.addDocumentListener(this)

			// listen for focus lost to stop editing
			TEXT_EDITOR.addFocusListener(this)

			editor.view.setCursor(Cursor.DEFAULT)
		}

		private fun stopEditing() {
			LOG.trace("stop editing")
			editor!!.commandManager.active = true
			val oldText = text.getTranslation()
			val newText = TEXT_EDITOR.text
			if (oldText != newText) {
				if (StringUtils.isNotEmpty(newText)) {
					LOG.userTrail("Change text of ${this@TextComponentJvm.id} to '${StringUtils.limit(newText, 30)}'")
					editor!!.commandManager.execute(
						TextChangeCommand(
							editor!!,
							this@TextComponentJvm.id,
							text,
							text.withTranslation(newText)
						)
					)
				} else {
					TEXT_EDITOR.text = oldText
				}
			}

			TEXT_EDITOR.document.removeDocumentListener(this)
			TEXT_EDITOR.removeFocusListener(this)

			(editor!!.view.canvas as Container).remove(TEXT_EDITOR)
			(editor!!.view.canvas as Container).revalidate()
			(editor!!.view.canvas as Container).repaint()

			editing = false
		}

		private fun requestLayoutTextEditor() {
			javax.swing.SwingUtilities.invokeLater { adjustTextEditor(editor!!) }
			editor!!.drawing.validate()
		}
	}
}