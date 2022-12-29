package bst.tree

class ParallelBst(val root: LockedNode = LockedNode(-1)) : Bst {

    fun traverse(searchVal: Int): Triple<LockedNode?, LockedNode?, LockedNode?> {
        var gprev: LockedNode?
        var prev: LockedNode?
        var curr: LockedNode?
        do {
            gprev = null
            prev = null
            curr = root
            while (
                curr !== null &&
                curr.value != searchVal &&
                (gprev?.isDeleted?.get() == false || prev?.isDeleted?.get() == false || !curr.isDeleted.get())
            ) {
                gprev = prev
                prev = curr
                curr = if (searchVal < curr.value) {
                    curr.left
                } else {
                    curr.right
                }
            }
        } while (
            gprev?.isDeleted?.get() == true ||
            prev?.isDeleted?.get() == true ||
            curr?.isDeleted?.get() == true
        )
        return Triple(gprev, prev, curr)
    }

    override fun insert(value: Int): Boolean {
        while (true) {
            val nodes = traverse(value)

            val curr = nodes.third
            val prev = nodes.second
            if (curr !== null) {
                if (!curr.isTransit) {
                    return false
                }

                val (writeLock, writeLockRes) = curr.writeLockState(true)
                if (!writeLockRes) {
                    println("Illegal unconditional read state lock")
                    writeLock.unlock()
                    continue
                } else {
                    curr.isTransit = false
                    writeLock.unlock()
                    return true
                }
            }
            val newNode = LockedNode(value)
            if (value < prev!!.value) {
                val (readStateLock, readStateLockRes) = prev.readLockState()
                if (!readStateLockRes) {
                    println("Illegal unconditional read state lock")
                    readStateLock.unlock()
                    continue
                } else {
                    val (leftLock, leftLockRes) = prev.lockLeftRef(null)
                    if (!leftLockRes) {
                        println("Illegal left lock")
                        leftLock.unlock()
                        readStateLock.unlock()
                        continue
                    }
                    prev.left = newNode
                    leftLock.unlock()
                    readStateLock.unlock()
                    return true
                }
            } else {
                val (readStateLock, readStateLockRes) = prev.readLockState()
                if (!readStateLockRes) {
                    println("Illegal unconditional read state lock")
                    readStateLock.unlock()
                    continue
                } else {
                    val (rightLock, rightLockRec) = prev.lockRightRef(null)
                    if (!rightLockRec) {
                        println("Illegal right lock")
                        rightLock.unlock()
                        readStateLock.unlock()
                        continue
                    }
                    prev.right = newNode
                    rightLock.unlock()
                    readStateLock.unlock()
                    return true
                }
            }
        }
    }

    override fun remove(value: Int): Boolean {
        while (true) {
            val nodes = traverse(value)
            val curr = nodes.third

            if (curr === null || curr.isTransit) {
                return false
            }

            if (curr.hasTwoChildren()) {
                if (!deleteIfTwoChildren(curr)) {
                    continue
                }
                return true
            }
            val prev = nodes.second!!
            if (curr.hasSingleChild()) {
                if (!deleteIfSingleChild(prev, curr)) {
                    continue
                }
                return true
            } else {
                if (!prev.isTransit) {
                    if (!deleteLeafWithNonTransitFather(prev, curr, value)) {
                        continue
                    }
                    return true
                } else {
                    val gprev = nodes.first!!
                    if (!deleteLeafWithTransitFather(gprev, prev, curr, value)) {
                        continue
                    }
                    return true
                }
            }
        }
    }

    override fun contains(value: Int): Boolean {
        val nodes = traverse(value)
        return nodes.third?.isTransit == false
    }

    fun isEqualToSeqBst(seqBst: SeqBst): Boolean {
        return root.isEqualToSeqNode(seqBst.root)
    }

    private fun deleteIfTwoChildren(curr: LockedNode): Boolean {
        val writeStateLockWithResult = curr.writeLockState(false)
        if (!writeStateLockWithResult.second) {
            println("Illegal write state lock")
            writeStateLockWithResult.first.unlock()
            return false
        } else if (!curr.hasTwoChildren()) {
            writeStateLockWithResult.first.unlock()
            return false
        } else {
            curr.isTransit = true
            writeStateLockWithResult.first.unlock()
            return true
        }
    }


    private fun deleteIfSingleChild(prev: LockedNode, curr: LockedNode): Boolean {
        val child = if (curr.left !== null) {
            curr.left!!
        } else {
            curr.right!!
        }
        if (curr.value < prev.value) {
            if (!lockNodeWithSingleChild(prev, curr, child) {
                    curr.isDeleted.set(true)
                    prev.left = child
                }) {
                return false
            }
            return true
        } else {
            if (!lockNodeWithSingleChild(prev, curr, child) {
                    curr.isDeleted.set(true)
                    prev.right = child
                }) {
                return false
            }
            return true
        }
    }

    private fun lockNodeWithSingleChild(
        prev: LockedNode,
        curr: LockedNode,
        child: LockedNode,
        action: () -> Unit
    ): Boolean {
        val (prevChildLock, prevChildLockRes) = prev.lockChildRef(curr)
        if (!prevChildLockRes) {
            println("Illegal child lock")
            prevChildLock.unlock()
            return false
        }
        val (writeLock, writeLockRes) = curr.writeLockState(false)
        if (!writeLockRes) {
            println("Illegal write state lock")
            writeLock.unlock()
            prevChildLock.unlock()
            return false
        }
        if (curr.hasTwoChildren() || curr.isLeaf()) {
            return false
        }
        val (childChildLock, childChildLockRes) = curr.lockChildRef(child)
        if (!childChildLockRes) {
            println("Illegal child child lock")
            childChildLock.unlock()
            writeLock.unlock()
            prevChildLock.unlock()
            return false
        }
        action()
        childChildLock.unlock()
        writeLock.unlock()
        prevChildLock.unlock()
        return true
    }

    private fun deleteLeafWithNonTransitFather(prev: LockedNode, curr: LockedNode, value: Int): Boolean {
        if (curr.value < prev.value) {
            if (!deleteLeafWithNonTransitFatherWithAction(prev, curr, value) { newCurrent ->
                    newCurrent.isDeleted.set(true)
                    prev.left = null
                }) {
                return false
            }
            return true
        } else {
            if (!deleteLeafWithNonTransitFatherWithAction(prev, curr, value) { newCurrent ->
                    newCurrent.isDeleted.set(true)
                    prev.right = null
                }) {
                return false
            }
            return true
        }
    }

    private fun deleteLeafWithNonTransitFatherWithAction(
        prev: LockedNode,
        curr: LockedNode,
        value: Int,
        action: (newCurrent: LockedNode) -> Unit
    ): Boolean {
        val (prevStateLock, prevStateLockRes) = prev.readLockState(false)
        if (!prevStateLockRes) {
            prevStateLock.unlock()
            return false
        }
        if (!lockLeaf(prev, curr, value, action)) {
            prevStateLock.unlock()
            return false
        }
        prevStateLock.unlock()
        return true
    }

    private fun deleteLeafWithTransitFather(
        gprev: LockedNode,
        prev: LockedNode,
        curr: LockedNode,
        value: Int
    ): Boolean {
        val child = if (curr.value < prev.value) {
            prev.right
        } else {
            prev.left
        }

        if (gprev.left !== null && prev === gprev.left) {
            if (!deleteLeafWithTransitFatherWithAction(gprev, prev, curr, child, value) { newCurrent ->
                    prev.isDeleted.set(true)
                    newCurrent.isDeleted.set(true)
                    gprev.left = child
                }) {
                return false
            }
        } else if (gprev.right !== null && prev === gprev.right) {
            if (!deleteLeafWithTransitFatherWithAction(gprev, prev, curr, child, value) { newCurrent ->
                    prev.isDeleted.set(true)
                    newCurrent.isDeleted.set(true)
                    gprev.right = child
                }) {
                return false
            }
        }
        return true
    }

    private fun deleteLeafWithTransitFatherWithAction(
        gprev: LockedNode,
        prev: LockedNode,
        curr: LockedNode,
        child: LockedNode?,
        value: Int,
        action: (newCurrent: LockedNode) -> Unit
    ): Boolean {
        val (gprevChildLock, gprevChildLockRes) = gprev.lockChildRef(prev)
        if (!gprevChildLockRes) {
            gprevChildLock.unlock()
            return false
        }
        val (prevWriteStateLock, prevWriteStateLockRes) = prev.writeLockState(true)
        if (!prevWriteStateLockRes) {
            prevWriteStateLock.unlock()
            gprevChildLock.unlock()
            return false
        }
        if (child !== null) {
            val (prevChildLock, prevChildLockRes) = prev.lockChildRef(child)
            if (!prevChildLockRes) {
                prevChildLock.unlock()
                prevWriteStateLock.unlock()
                gprevChildLock.unlock()
                return false
            }
            if (!lockLeaf(prev, curr, value, action)) {
                prevChildLock.unlock()
                prevWriteStateLock.unlock()
                gprevChildLock.unlock()
                return false
            } else {
                prevChildLock.unlock()
                prevWriteStateLock.unlock()
                gprevChildLock.unlock()
                return true
            }
        } else {
            if (!lockLeaf(prev, curr, value, action)) {
                prevWriteStateLock.unlock()
                gprevChildLock.unlock()
                return false
            } else {
                prevWriteStateLock.unlock()
                gprevChildLock.unlock()
                return true
            }
        }
    }

    private fun lockLeaf(
        prev: LockedNode,
        curr: LockedNode,
        value: Int,
        action: (newCurrent: LockedNode) -> Unit
    ): Boolean {
        val (prevChildLock, prevChildLockRes) = prev.lockChildValue(curr)
        if (!prevChildLockRes) {
            prevChildLock.unlock()
            return false
        }
        val newCurrent = if (value < prev.value) {
            prev.left
        } else {
            prev.right
        }
        val (newCurrWriteStateLock, newCurrWriteStateLockRes) = newCurrent!!.writeLockState(false)
        if (!newCurrWriteStateLockRes) {
            newCurrWriteStateLock.unlock()
            prevChildLock.unlock()
            return false
        }
        if (!curr.isLeaf()) {
            return false
        }
        action(newCurrent)
        newCurrWriteStateLock.unlock()
        prevChildLock.unlock()
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParallelBst

        if (root != other.root) return false

        return true
    }

    override fun hashCode(): Int {
        return root.hashCode()
    }

    override fun toString(): String {
        return "ParallelBst(root=${root.deepToString()})"
    }
}