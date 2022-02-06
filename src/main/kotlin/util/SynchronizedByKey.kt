package util

import java.util.concurrent.ConcurrentHashMap

/**
 * 通过key进行同步
 */
internal class SynchronizedByKey {

    //保存互斥锁
    private val keyCache = ConcurrentHashMap<String, Any>()

    fun exec(key: String, block: () -> Unit) {

        //获取互斥key
        //相当于 通过key获取map的value 如果为null 则创建一个加到map后 重新返回  且ConcurrentHashMap内部有直接保证线程安全
        val mutexKey = keyCache.computeIfAbsent(key) { Any() }

        synchronized(mutexKey) {

            //判断是否存在mutexKey无效的情况,重新进行获取key执行
            val cacheMutexKey = keyCache[key]
            if (cacheMutexKey == null || cacheMutexKey != mutexKey) {
                exec(key, block)
                return
            }

            try {
                block()
            } finally {
                keyCache.remove(key)
            }
        }
    }
}