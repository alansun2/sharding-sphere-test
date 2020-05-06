package com.example.demo.utils

import com.google.common.base.Preconditions
import lombok.SneakyThrows
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator
import java.time.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author AlanSun
 * @date 2020/4/29 14:52
 *
 */
class MySnowFlakeShardingKeyGenerator : ShardingKeyGenerator {
    private var EPOCH: Long = 0

    private val SEQUENCE_BITS = 13L

    private val WORKER_ID_BITS = 10L

    private val SEQUENCE_MASK = (1 shl SEQUENCE_BITS.toInt()) - 1

    private val WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS

    private val TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS

    private val WORKER_ID_MAX_VALUE = 1L shl WORKER_ID_BITS.toInt()

    private val SEQUENCE_MAX_VALUE = 1L shl SEQUENCE_BITS.toInt()

    private val WORKER_ID: Long = 0

    private val DEFAULT_VIBRATION_VALUE = 1

    private val MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 1

    init {
        EPOCH = ZonedDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0, 0, 0), ZoneOffset.systemDefault()).toInstant().toEpochMilli()
    }

    private var lastMilliseconds: Long = 0

    private var lastIncrSequence: Int = 0

    private var sequence: AtomicInteger = AtomicInteger(-1)

    private var propertie = Properties()

    override fun setProperties(properties: Properties?) {
        if (properties != null) {
            propertie = properties
        }
    }

    override fun getType(): String {
        return "MY-SNOWFLAKE"
    }

    override fun getProperties(): Properties {
        return propertie

    }

    override fun generateKey(): Comparable<*> {
        var currentMilliseconds = Instant.now().toEpochMilli()
        if (waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
            currentMilliseconds = Instant.now().toEpochMilli()
        }

        // 序列自增，用于分库分表
        var currentSequence = sequence.incrementAndGet()
        if (currentSequence >= SEQUENCE_MAX_VALUE) {
            sequence.set(-1)
            currentSequence = 0
        }

        // 毫秒时间戳相同 && 序列号相同，可能有重复
        if (lastMilliseconds == currentMilliseconds && lastIncrSequence == currentSequence) {
            currentMilliseconds = waitUntilNextTime(currentMilliseconds)
        }

        lastMilliseconds = currentMilliseconds
        return currentMilliseconds - EPOCH shl TIMESTAMP_LEFT_SHIFT_BITS.toInt() or (getWorkerId() shl WORKER_ID_LEFT_SHIFT_BITS.toInt()) or currentSequence.toLong()
    }

    /**
     * 判断是否有时间回拨
     */
    @SneakyThrows
    private fun waitTolerateTimeDifferenceIfNeed(currentMilliseconds: Long): Boolean {
        if (lastMilliseconds <= currentMilliseconds) {
            return false
        }

        val timeDifferenceMilliseconds: Long = lastMilliseconds - currentMilliseconds
        // 时间回拨容忍度 1 毫秒
        Preconditions.checkState(timeDifferenceMilliseconds < MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS,
                "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastMilliseconds, currentMilliseconds)
        Thread.sleep(timeDifferenceMilliseconds)
        return true
    }

    private fun getWorkerId(): Long {
        val result = properties.getProperty("worker.id", WORKER_ID.toString()).toLong()
        Preconditions.checkArgument(result in 0L until WORKER_ID_MAX_VALUE)
        return result
    }

    /**
     * 获取真正可用的下一毫秒
     *
     * 应该很少出现，按 13位来算的话，理论上一秒支持 819.2 万订单
     *
     * @param lastTime 有重复订单号时的毫秒
     */
    private fun waitUntilNextTime(lastTime: Long): Long {
        var availableNextMills = Instant.now().toEpochMilli()
        while (availableNextMills <= lastTime) {
            availableNextMills = Instant.now().toEpochMilli()
        }
        return availableNextMills
    }
}