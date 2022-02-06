import util.SynchronizedByKey

/**
 * 内存
 *
 * 读写每次一个字节 即8位 最大256
 *
 * 考虑线程安全？？？  同一个地址，只能有一个写入
 */
class Memory {

    /**
     * 64KB
     */
    private val memory = ByteArray(1024 * 64)

    /**
     * 通过key进行同步
     */
    private val synchronizedByKey = SynchronizedByKey()

    /**
     * 读
     */
    fun readMemoryUnsafe8(address: Int) = memory[address]

    /**
     * 写
     */
    fun writeMemoryUnsafe8(address: Int, value: Byte) {
        memory[address] = value
    }

    /**
     * 写  安全
     */
    fun writeMemorySafe8(address: Int, value: Byte) {

        //保证线程安全 同一个地址的写操作 进行加锁等待处理
        synchronizedByKey.exec(address.toString()) {
            memory[address] = value
        }
    }


    /**
     * 下面使用CAS 全程保证线程安全
     */
//    /**
//     * 64KB
//     */
//    private val memoryArray = AtomicReferenceArray(Array<Byte>(1024 * 64) { 0 })
//
//    /**
//     * 读
//     */
//    fun readMemorySafe8(address: Int): Byte = memoryArray[address]
//
//    /**
//     * 写
//     */
//    fun writeMemorySafe8(address: Int, data: Byte) {
//        // 使用CAS 保证线程安全
//        memoryArray.compareAndSet(address, memoryArray[address], data)
//    }
}