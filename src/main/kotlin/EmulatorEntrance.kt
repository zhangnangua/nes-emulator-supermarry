import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Toolkit
import java.awt.event.ActionListener
import java.awt.geom.AffineTransform
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel


/**
 * 模拟器入口
 */
fun main() {
    val content = readByteContentFromRom("rom\\rom.nes")

    //程序运行
    val pc = MotherBoard().apply {
        //赋值卡带
        card = content
        //程序启动
        init()
    }
    //窗体绘制
    frameDisplay(pc)
    // 开机
    pc.start()

}

/**
 * 以二进制的形式读取rom文件
 */
fun readByteContentFromRom(filePath: String): ByteArray? {
    val file = File(filePath)
    var totalLength = file.length()
    if (file.exists()) {
        val buffer = ByteArray(1024 * 2)
        val byteArrayOutputStream = ByteArrayOutputStream()
        file.inputStream().use { fileInputStream ->
            while (totalLength > 0) {
                val read = fileInputStream.read(buffer, 0, buffer.size)
                if (read < 0) {
                    break
                }
                byteArrayOutputStream.use {
                    it.write(buffer, 0, read)
                }
                totalLength -= read
            }
            return byteArrayOutputStream.toByteArray()
        }
    }
    return null
}

/**
 * 窗体显示
 */
fun frameDisplay(pc: MotherBoard) {
    val title = "PUMPKIN"
    val frame = JFrame(title)
    // 设置窗体属性
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(1050 + 16, 1000 + 16 + 25)
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val x = (screenSize.getWidth().toInt() - frame.width) / 2
    val y = (screenSize.getHeight().toInt() - frame.height) / 2
    frame.setLocation(x, y)

    // 主Panel，用于显示图形画页
    val mainPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val img: RenderedImage? = pc.getRenderedImage()
            if (img != null) (g as Graphics2D).drawRenderedImage(img, AffineTransform.getScaleInstance(4.0, 4.0))
        }
    }
    //刷新界面
    pc.frameRefresh = ActionListener {
        mainPanel.repaint()
    }
    mainPanel.setLocation(10, 10)
    mainPanel.setSize(frame.width - 10 - 25, frame.height - 25 - 35)
    frame.add(mainPanel)
    //按键监听
    frame.addKeyListener(pc.joyPad)
    //title 刷新
    Thread {
        while (true) {
            Thread.sleep(1000)
            frame.title = "$title     ${pc.obtainFPSCount()} FPS"
        }
    }.start()

    frame.isVisible = true
}
