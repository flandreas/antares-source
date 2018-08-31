package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import java.awt.BasicStroke
import java.awt.Component
import java.awt.Container
import java.awt.Graphics
import java.util.*
import javax.swing.JTextPane
import javax.swing.border.LineBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/** Implements [TextComponentFactory] on the JVM platform. */
class TextComponentFactoryJvm : TextComponentFactory {

    override fun create(text: String, location: Point2D, styleType: StyleType, styleProvider: StyleProvider): TextComponent {
        return TextComponentJvm(text, location, styleType, styleProvider)
    }
}

/**
 * A [Component] that supports inline text editing using JDK classes.
 */
open class TextComponentJvm(
        text: String,
        location: Point2D,
        styleType: StyleType,
        styleProvider: StyleProvider
) : AbstractRectangularComponent(styleType = styleType, styleProvider = styleProvider, shape = Rectangle2D(location.x, location.y, 0.0, 0.0)),
    Transparent, TextComponent {

    constructor(text: String): this(text = text, location = Point2D.ZERO, styleType = StyleType.FIGURE, styleProvider = DrawStyleModule.styleProvider)
    @Suppress("unused") constructor(): this("")

    private companion object {

	    private val LOG by logger(TextComponentJvm::class)

	    /** The horizontal inset between the bounding box and the text.  */
        private const val INSET_X = 10

        /** The vertical inset between the bounding box and the text.  */
        private const val INSET_Y = 10

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

    override var text: String = text
        set(value) {
            invalidate()
            field = value
            invalidate()
            validate()
        }

	var textProperty: TextProperty
		get() = TextProperty(text)
		set(value) { text = value.text ?: "" }

    private val eventHandler = EventHandler()

    /** ---- [Transparent] */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) {
            transparent.transparency = value
        }

    private var decorator: TextComponentDecorator = RectangularShapeTextComponentDecorator(
        shape = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 20.0, 20.0),
        stylable = this,
        transparent = transparent
    )

    init {
        adjustBounds()
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("text", text)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        text = reader.readString("text")
    }

    /** ---- [Drawable] */

    override fun contains(x: Double, y: Double): Boolean {
        return super<AbstractRectangularComponent>.contains(x, y)
    }

    override fun contains(p: Point2D): Boolean {
        return super<AbstractRectangularComponent>.contains(p)
    }

    override fun draw(context: DrawContext) {
        val oldClip = context.g.getClipBounds()
        val b = shape

        if (filled) {
            decorator.drawBackground(this, context)
        }

        setupTextPainter(context)
        (context.g as Graphics2DJvm).g.setClip(b.x.toInt(), b.y.toInt(), b.width.toInt(), b.height.toInt())
        context.g.translate(TEXT_PAINTER.x.toDouble(), TEXT_PAINTER.y.toDouble())
        TEXT_PAINTER.paint((context.g as Graphics2DJvm).g)
        context.g.translate(-TEXT_PAINTER.x.toDouble(), -TEXT_PAINTER.y.toDouble())
        (context.g as Graphics2DJvm).g.setClip(oldClip.x.toInt(), oldClip.y.toInt(), oldClip.width.toInt(), oldClip.height.toInt())

        if (stroked) {
            decorator.drawForeground(this, context)
        }
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return eventHandler as InputEventHandler<T>
    }

    /** ---- [Component] interface */

    override val type: String? get() = Translations.getString("edit.component.text")

    /** ---- [TextComponent] */

    /**
     * Adjusts the size of this [TextComponent]'s shape to the preferred size of the current text, expanded by
     * the constant horizontal and vertical insets.
     *
     * Currently, this method only changes the width and height of the shape, but not its position. It should be
     * considered whether or not this method should also change the position of the shape, depending on the current
     * alignment settings.
     */
    private fun adjustBounds() {
        // TODO change the position of the bounding box depending on alignment?
        // Note that TextEditTool uses its own adjustment strategy for inline editing,
        // which does change the position depending on alignment
        val b = shape
        val dim = measureText(text, Graphics2DJvm.toAwtFont(font), 1.0)
        setFrame(b.x, b.y, dim.width + 2 * INSET_X, dim.height + 2 * INSET_Y)
    }

    /** Adjust the size of the currently used text editor. */
    private fun adjustTextEditor(editor: Editor) {
        val bounds = shape
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
    private fun setupTextPainter(context: DrawContext) {
        val attr = SimpleAttributeSet()
        val awtFont = Graphics2DJvm.toAwtFont(font)

        StyleConstants.setFontFamily(attr, awtFont.family)
        StyleConstants.setFontSize(attr, awtFont.size)
        StyleConstants.setBold(attr, awtFont.isBold)
        StyleConstants.setItalic(attr, awtFont.isItalic)
        StyleConstants.setForeground(attr, Graphics2DJvm.toAwtColor(
	        if (context.useContextColors) context.color!!.textColor else transparent.applyTo(color.textColor)))
        StyleConstants.setBackground(attr, Graphics2DJvm.toAwtColor(
	        if (context.useContextColors) context.color!!.backgroundColor else transparent.applyTo(color.backgroundColor)))
        StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT)

        val bounds = shape
        TEXT_PAINTER.setBounds(
                bounds.x.toInt() + INSET_X,
                bounds.y.toInt() + INSET_Y,
                bounds.width.toInt() - 2 * INSET_X,
                Integer.MAX_VALUE)
        TEXT_PAINTER.text = text
        TEXT_PAINTER.selectAll()
        TEXT_PAINTER.setParagraphAttributes(attr, true)
    }

    private fun setupTextEditor(zoomFactor: Double) {
        val attr = SimpleAttributeSet()
        val awtFont = Graphics2DJvm.toAwtFont(font)

        StyleConstants.setFontFamily(attr, awtFont.family)
        StyleConstants.setFontSize(attr, (awtFont.size * zoomFactor).toInt())
        StyleConstants.setBold(attr, awtFont.isBold)
        StyleConstants.setItalic(attr, awtFont.isItalic)
        StyleConstants.setForeground(attr, java.awt.Color.BLACK)
        StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT)

        TEXT_EDITOR.text = ""
        TEXT_EDITOR.setParagraphAttributes(attr, true)
        TEXT_EDITOR.text = text
    }

    /**
     * [TextFieldBorder] draws an orange, dashed lines around the text editor and provides a graphical feedback
     * that the editor is currently active.
     */
    private class TextFieldBorder : LineBorder(java.awt.Color.ORANGE, 1) {
        companion object {
            val STROKE = BasicStroke(0.1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, floatArrayOf(5f, 5f ), 0f)
        }

        override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
            (g as java.awt.Graphics2D).stroke = STROKE
            super.paintBorder(c, g, x, y, width, height)
        }
    }

    private inner class EventHandler : InputEventHandlerAdapter<EditInputEventContext>(), DocumentListener {

        private var editor: Editor? = null
        private var editing: Boolean = false
        private var oldText: String? = null

        /** ----  */

        override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            return if (editing) this else null
        }

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            if (context.mouseEvent!!.clickCount == 2) {
                startEditing(context.editor, context.mouseEvent)
                return this
            }
            if (editing) {
                stopEditing()
            }
            return null
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

        private fun startEditing(editor: Editor, event: MouseEvent) {
            LOG.debug("TextComponent: start editing")
            this.editor = editor
            editing = true
            oldText = text

            setupTextEditor(editor.view.zoomFactor)
            adjustTextEditor(editor)

            (editor.view.canvas as Container).add(TEXT_EDITOR)
            (editor.view.canvas as Container).revalidate()
            (editor.view.canvas as Container).repaint()

            // request focus and position caret within the text
            TEXT_EDITOR.dispatchEvent(
                    java.awt.event.MouseEvent(
                            TEXT_EDITOR,
                            java.awt.event.MouseEvent.MOUSE_PRESSED,
                            Date().time,
                            event.modifiers,
                            event.x - TEXT_EDITOR.bounds.getX().toInt(),
                            event.y - TEXT_EDITOR.bounds.getY().toInt(),
                            event.clickCount,
                            false))

            // listen for text changes in order to adjust the editors size
            TEXT_EDITOR.document.addDocumentListener(this)

            editor.view.setCursor(Cursor.DEFAULT)
        }

        private fun stopEditing() {
            LOG.debug("TextComponent: stop editing")
            if (oldText != TEXT_EDITOR.text) {
                editor!!.commandManager.execute(
                    TextChangeCommand(editor!!, this@TextComponentJvm, oldText!!, TEXT_EDITOR.text))
            }

            TEXT_EDITOR.document.removeDocumentListener(this)

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