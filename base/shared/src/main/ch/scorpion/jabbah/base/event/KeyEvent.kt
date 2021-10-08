package ch.scorpion.jabbah.base.event

enum class KeyEventType {
	TYPED, PRESSED, RELEASED
}
/**
 * An event which indicates that the user has pressed a key.
 */
interface KeyEvent : InputEvent {

	val type: KeyEventType

    val key: Int

	val keyChar: Char

    companion object {

        /**
         * The following key codes are target specific and must be set in the setup of the corresponding Target system.
         * The codes given here are completely arbitrary and only used for testing of 'common' code.
         */

        var VK_LEFT = 0x01
        var VK_RIGHT = 0x02
	    var VK_UP = 0x03
	    var VK_DOWN = 0x04
        var VK_ESCAPE = 0x05
        var VK_ENTER = 0x06
	    var VK_ALT = 0x07
	    var VK_DELETE = 0x08
	    var VK_0 = 0x09
	    var VK_1 = 0x0A
	    var VK_2 = 0x0B
	    var VK_3 = 0x0C
	    var VK_4 = 0x0D
	    var VK_5 = 0x0E
	    var VK_6 = 0x0F
	    var VK_7 = 0x10
	    var VK_8 = 0x11
	    var VK_9 = 0x12
	    var VK_A = 0x13
	    var VK_B = 0x14
	    var VK_C = 0x15
	    var VK_D = 0x16
	    var VK_E = 0x17
	    var VK_F = 0x18
    }
}

/**
 * A platform-independent empty implementation of [KeyEvent] to be used when applying the "null pattern",
 * or for testing.
 */
data class KeyEventImpl(
	override val type: KeyEventType,
	override val event: Any = "",
	override val key: Int,
	override val keyChar: Char,
	override val source: Any = "",
	override val modifiers: Int = 0
) : KeyEvent {

	private var consumed: Boolean = false

	override fun consume() {
		consumed = true
	}

	override fun isConsumed(): Boolean = consumed
}

interface KeyListener {
    fun keyTyped(e: KeyEvent)
    fun keyPressed(e: KeyEvent)
    fun keyReleased(e: KeyEvent)
}

/**
 * An empty implementation of the [KeyListener] interface intended to be subclassed by classes that
 * only need to implement some of the [KeyListener] event handling methods.
 */
open class KeyAdapter : KeyListener {
    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {}
    override fun keyReleased(e: KeyEvent) {}
}

