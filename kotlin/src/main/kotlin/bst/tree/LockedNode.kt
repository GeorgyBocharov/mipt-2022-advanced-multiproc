package bst.tree

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock

class LockedNode(var value: Int, var isTransit: Boolean = false) {
    var left: LockedNode? = null
    var right: LockedNode? = null
    val isDeleted = AtomicBoolean(false)

    private val leftLock = ReentrantReadWriteLock()
    private val rightLock = ReentrantReadWriteLock()
    private val stateLock = ReentrantReadWriteLock()

    fun lockChildRef(expected: LockedNode): Pair<Lock, Boolean> {
        return if (expected.value < this.value) {
            lockLeftRef(expected)
        } else {
            lockRightRef(expected)
        }
    }

    fun lockChildValue(expected: LockedNode): Pair<Lock, Boolean> {
        return if (expected.value < this.value) {
            lockValue(leftLock.writeLock(), left, expected)
        } else {
            lockValue(rightLock.writeLock(), right, expected)
        }
    }

    fun lockLeftRef(expected: LockedNode?): Pair<Lock, Boolean> {
        val writeLock = leftLock.writeLock()
        writeLock.lock()
        var success = true
        if (isDeleted.get() || expected !== left) {
            success = false
        }
        return Pair(writeLock, success)
    }

    fun lockRightRef(expected: LockedNode?): Pair<Lock, Boolean> {
        val writeLock = rightLock.writeLock()
        writeLock.lock()
        var success = true
        if (isDeleted.get() || expected !== right) {
            success = false
        }
        return Pair(writeLock, success)
    }

    fun readLockState(): Pair<Lock, Boolean> {
        val stateLock = stateLock.readLock()
        stateLock.lock()
        var success = true

        if (isDeleted.get()) {
            success = false
        }
        return Pair(stateLock, success)
    }

    fun readLockState(expectedTransitFlag: Boolean): Pair<Lock, Boolean> {
        val stateLock = stateLock.readLock()
        stateLock.lock()
        var success = true

        if (isDeleted.get() || isTransit != expectedTransitFlag) {
            success = false
        }
        return Pair(stateLock, success)
    }

    fun writeLockState(expectedTransitFlag: Boolean): Pair<Lock, Boolean> {
        val stateLock = stateLock.writeLock()
        stateLock.lock()
        var success = true
        if (isDeleted.get() || isTransit != expectedTransitFlag) {
            success = false
        }
        return Pair(stateLock, success)
    }

    fun isLeaf(): Boolean {
        return left === null && right === null
    }

    fun hasTwoChildren(): Boolean {
        return left !== null && right !== null
    }

    fun hasSingleChild(): Boolean {
        return (left !== null && right === null) || (right !== null && left === null)
    }

    fun isEqualToSeqNode(seqNode: Node?): Boolean {
        if (seqNode === null) {
            return false
        }
        return seqNode.value == value &&
                seqNode.isTransit == isTransit &&
                ((left === null && seqNode.left === null) || (left !== null && left!!.isEqualToSeqNode(seqNode.left))) &&
                ((right === null && seqNode.right === null) || (right !== null && right!!.isEqualToSeqNode(seqNode.right)))
    }

    private fun lockValue(writeLock: WriteLock, actual: LockedNode?, expected: LockedNode): Pair<Lock, Boolean> {
        writeLock.lock()
        var success = true
        if (isDeleted.get() || actual === null || actual.value != expected.value) {
            success = false
        }
        return Pair(writeLock, success)
    }


    override fun toString(): String {
        return "LockedNode(value=$value, isTransit=$isTransit, leftVal=${left?.value}, rightVal=${right?.value})"
    }

    fun deepToString(): String {
        return "LockedNode(value=$value, isTransit=$isTransit, left=${left?.deepToString()}, right=${right?.deepToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LockedNode

        if (value != other.value) return false
        if (isTransit != other.isTransit) return false
        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + isTransit.hashCode()
        result = 31 * result + (left?.hashCode() ?: 0)
        result = 31 * result + (right?.hashCode() ?: 0)
        return result
    }


}