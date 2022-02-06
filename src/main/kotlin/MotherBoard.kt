import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine


/**
 * 主板
 *
 *
 * 理出我们实际使用的地址
 * ----名称----地址
 * ----显卡----0x2000~0x2007 0x4014
 * ----声卡----0x4000~0x4015
 * ----游戏手柄----0x4016~0x4017
 * ----游戏卡带----0x8000~0xFFFF
 * ----内存----0x6000~0x7FFF
 */
class MotherBoard {

    /**
     * CPU、显卡、内存、声卡、手柄
     */
    private lateinit var cpu: CPUJava
    private lateinit var videoCard: VideoCardJava
    private lateinit var memory: Memory
    private lateinit var audioCard: AudioCardJava
    lateinit var joyPad: JoyPad

    /**
     * 卡带，即赋值加载进来的rom
     */
    var card: ByteArray? = null

    /**
     * 帧数据输出监听者
     */
    var frameRefresh: ActionListener? = null

    /**
     * fps帧数统计
     */
    private var frameCount = 0

    /**
     * 存放声音数组
     */
    private val audioQueue: Queue<ByteArray> = LinkedList()

    /**
     * init
     */
    fun init() {
        cpu = CPUJava(this)
        videoCard = VideoCardJava(this)
        memory = Memory()
        audioCard = AudioCardJava(this)
        joyPad = JoyPad()
    }

    /**
     * 启动
     */
    fun start() {
        //初始化声卡
        initSoundCard()
        //读取程序启动点  即pc_register储存下一步需要执行的指令 首先将启动点的指令赋值给pc_register
        cpu.pc_register = cpu.ReadBus16(0xFFFC)
        cpu.RunProcessor()
    }

    /**
     * 从各个组成读取字节
     */
    fun readBus8(address: Int): Byte {
        return if (address in 0x2000..0x2007)
            videoCard.read(address)
        else if (address in 0x4000..0x4015)
            audioCard.read(address)
        else if (address in 0x4016..0x4017)
            joyPad.read(address)
        else if (address >= 0x8000)
        //-0x8000是扣掉现在的位置，因为rom是从0开始的，+16，是因为rom的前16字节是定义信息，之后才是代码信息，可以查rom表信息。
            card?.get(address - 0x8000 + 16) ?: 0
        else
            memory.readMemoryUnsafe8(address)
    }

    /**
     * 从各个组成写字节
     */
    fun writeBus8(address: Int, data: Byte) {
        //写入硬盘、内存、游戏手柄、显卡、声卡
        when (address) {
            in 0x2000..0x2007 -> videoCard.write(address, data)
            0x4014 -> videoCard.write(address, data)
            in 0x4000..0x4013 -> audioCard.write(address, data)
            0x4015 -> audioCard.write(address, data)
            in 0x4016..0x4017 -> joyPad.write(address, data)
            else -> memory.writeMemoryUnsafe8(address, data)
        }
    }

    /**
     * cpu调用
     */
    var tickCount: Long = 0
    var totalCycles: Long = 0
    fun cpuTick(tickCount: Long): Long {
        var tickCountNum = tickCount
        this.tickCount = tickCount
        totalCycles += tickCount
        // 113 for NTSC, 106 for PAL
        if (tickCountNum >= 113) {
            if (videoCard.RenderNextScanline()) {
                cpu.Push16(cpu.pc_register)
                cpu.PushStatus()
                cpu.pc_register = cpu.ReadBus16(0xFFFA)
                //图像获取绘制
                renderFrame()
                //声音
                val render = audioCard.Render(tickCount)
                audioQueue.add(render)
            }
            tickCountNum -= 113
        }
        return tickCountNum
    }

    /**
     * 显示60FPS
     */
    private var _lastFrameTime = 0.0
    private val framePeriod = 0.01667 * 1000 // 60 FPS

    /**
     * 每帧显示的数据
     */
    private var newArray: ShortArray? = null

    /**
     * 渲染图像处理  首先处理FPS 最大为60
     */
    private fun renderFrame() {
        // 卡住，让快速的CPU慢下来，保证60 fps
        while (true) {
            if (System.currentTimeMillis() - _lastFrameTime >= framePeriod) break
        }
        _lastFrameTime = System.currentTimeMillis().toDouble()
        newArray = videoCard.offscreenBuffer
        // 通知外部取出该帧数据
        if (frameRefresh != null)
            frameRefresh!!.actionPerformed(null)

        frameCount++
    }

    /**
     * 取得一帧显示图象对象
     */
    fun getRenderedImage(): RenderedImage? {
        if (newArray != null) {
            val width = 256
            val height = 224
            val bi = BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB)
            val raster = bi.raster
            raster.setDataElements(0, 0, width, height, newArray)
            return bi
        }
        return null
    }

    /**
     * 获取FPS帧数
     */
    fun obtainFPSCount(): Int {
        val ret = frameCount
        frameCount = 0
        return ret
    }

    /**
     * 声卡初始化
     */
    private fun initSoundCard() {
        val rate = 44100F
        val sampleSize = 16
        val bigEndian = false
        val channels = 1
        val encoding = AudioFormat.Encoding.PCM_SIGNED
        val audioFormat = AudioFormat(
            encoding, rate, sampleSize, channels, sampleSize / 8 * channels, rate,
            bigEndian
        )
        try {
            // 设置数据输入
            val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
            val sourceDataLine = AudioSystem.getLine(info) as SourceDataLine
            sourceDataLine.open(audioFormat)
            sourceDataLine.start()
            playBuffer(sourceDataLine)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    /**
     * 播放处理
     */
    private fun playBuffer(sourceDataLine: SourceDataLine) {
        Thread {
            while (true) {
                if (audioQueue.size == 0) {
                    try {
                        Thread.sleep(1)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    continue
                }
                val dt: ByteArray = audioQueue.poll()
                // 播放
                sourceDataLine.write(dt, 0, dt.size)
            }
        }.start()
    }
}