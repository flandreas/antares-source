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
	    var VK_SPACE = 0x09
	    var VK_0 = 48
	    var VK_1 = 49
	    var VK_2 = 50
	    var VK_3 = 51
	    var VK_4 = 52
	    var VK_5 = 52
	    var VK_6 = 54
	    var VK_7 = 55
	    var VK_8 = 56
	    var VK_9 = 57
	    var VK_A = 65
	    var VK_B = 66
	    var VK_C = 67
	    var VK_D = 68
	    var VK_E = 69
	    var VK_F = 70
	    var VK_X = 88
	    var VK_Z = 90
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

