import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import kotlin.experimental.and

/**
 * 手柄
 * 一个byte,8位。用每一位来代表一个键,1表示按下,0表示没按下.
 */
class JoyPad : KeyListener {

    private var joyData = 0
    private var joyPadData = 0
    private var joyStrobe: Byte = 0

    fun read(address: Int): Byte {
        return when (address) {
            0x4016, 0x4017 -> {
                val num = (joyData and 1).toByte()
                joyData = joyData shr 1
                num
            }
            else -> {
                0
            }
        }
    }

    fun write(address: Int, value: Byte) {
        if (address == 0x4016) {
            if (joyStrobe.toInt() == 1 && (value and 1).toInt() == 0) {
                this.joyData = joyPadData or 0x100  //256 0001 0000 0000
            }
            joyStrobe = (value and 1)
        }
    }

    override fun keyTyped(e: KeyEvent?) {

    }

    override fun keyPressed(e: KeyEvent?) {
        val key = readKey(e!!.keyCode)
        if (key > 0) {
            joyPadData = joyPadData or key
        }
    }

    override fun keyReleased(e: KeyEvent?) {
        val dt1: Int = readKey(e!!.keyCode)
        if (dt1 > 0) {
            joyPadData = joyPadData xor dt1
        }
    }

    /**
     * 输入key  转换为位数
     */
    private fun readKey(key: Int): Int {
        return when (key) {
            //K(A)  00000001
            75 -> 0x01
            //J(B)  00000010
            74 -> 0x02
            //V(select)  00000100
            86 -> 0x04
            //B(select)  00001000
            66 -> 0x08
            //W/(UP)  00010000
            87, 38 -> 0x10
            //S/(DOWN)  00100000
            83, 40 -> 0x20
            //A/(LEFT)  01000000
            65, 37 -> 0x40
            //D/(RIGHT)  10000000
            68, 39 -> 0x80
            else -> -1
        }
    }


}