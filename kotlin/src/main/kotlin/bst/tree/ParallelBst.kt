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
                    println("Illegal unconditional write state lock: ${Thread.currentThread().id}")
                    writeLock.unlock()
                    continue
                } else {
                    curr.isTransit = false
                    writeLock.unlock()
                    return true
                }
            }
            val newNode = LockedNode(value)
            val (readStateLock, readStateLockRes) = prev!!.readLockState()
            if (!readStateLockRes) {
                println("Illegal unconditional read state lock: ${Thread.currentThread().id}")
                readStateLock.unlock()
                continue
            } else if (value < prev.value) {
                println("Inserting to the left: ${Thread.currentThread().id}")
                val (leftLock, leftLockRes) = prev.lockLeftRef(null)
                if (!leftLockRes) {
                    println("Illegal left lock: ${Thread.currentThread().id}")
                    leftLock.unlock()
                    readStateLock.unlock()
                    println("Inserting to the left failed: ${Thread.currentThread().id}")
                    continue
                }
                prev.left = newNode
                leftLock.unlock()
                readStateLock.unlock()
                println("Inserting to the left Success: ${Thread.currentThread().id}")
                return true
            } else {
                println("Inserting to the right: ${Thread.currentThread().id}")
                val (rightLock, rightLockRec) = prev.lockRightRef(null)
                if (!rightLockRec) {
                    println("Illegal right lock: ${Thread.currentThread().id}")
                    rightLock.unlock()
                    readStateLock.unlock()
                    println("Inserting to the right failed: ${Thread.currentThread().id}")
                    continue
                }
                prev.right = newNode
                rightLock.unlock()
                readStateLock.unlock()
                println("Inserting to the right success: ${Thread.currentThread().id}")
                return true
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
                println("Attempt to delete with 2 children: ${Thread.currentThread().id}")
                if (!deleteIfTwoChildren(curr)) {
                    println("Failed to delete with 2 children: ${Thread.currentThread().id}")
                    continue
                }
                println("Success deletion of 2 children: ${Thread.currentThread().id}")
                return true
            }
            val prev = nodes.second!!
            if (curr.hasSingleChild()) {
                println("Attempt to delete with 1 child: ${Thread.currentThread().id}")
                if (!deleteIfSingleChild(prev, curr)) {
                    println("Failed to delete with 1 child: ${Thread.currentThread().id}")
                    continue
                }
                println("Success deletion of 1 child: ${Thread.currentThread().id}")
                return true
            }
            if (!prev.isTransit) {
                println("Attempt to delete leaf with non transit father: ${Thread.currentThread().id}")
                if (!deleteLeafWithNonTransitFather(prev, curr, value)) {
                    println("Failed delete leaf with non transit father: ${Thread.currentThread().id}")
                    continue
                }
                println("Success delete leaf with non transit father: ${Thread.currentThread().id}")
                return true
            } else {
                val gprev = nodes.first!!
                println("Attempt to delete leaf with non transit father: ${Thread.currentThread().id}")
                if (!deleteLeafWithTransitFather(gprev, prev, curr, value)) {
                    println("Failed delete leaf with transit father: ${Thread.currentThread().id}")
                    continue
                }
                println("Success delete leaf with transit father: ${Thread.currentThread().id}")
                return true
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
        val (writeStateLock, writeStateLockRes) = curr.writeLockState(false)
        if (!writeStateLockRes) {
            println("deleteIfTwoChildren Illegal writeStateLock $curr: ${Thread.currentThread().id}")
            writeStateLock.unlock()
            return false
        } else if (!curr.hasTwoChildren()) {
            writeStateLock.unlock()
            println("deleteIfTwoChildren curr hasn't 2 children anymore $curr: ${Thread.currentThread().id}")
            return false
        } else {
            curr.isTransit = true
            writeStateLock.unlock()
            return true
        }
    }


    private fun deleteIfSingleChild(prev: LockedNode, curr: LockedNode): Boolean {
        val child = if (curr.left !== null) {
            curr.left
        } else {
            curr.right
        }
        if (child === null) {
            return false
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
            println("lockNodeWithSingleChild: Illegal child lock: ${Thread.currentThread().id}")
            prevChildLock.unlock()
            return false
        }
        val (writeLock, writeLockRes) = curr.writeLockState(false)
        if (!writeLockRes) {
            println("lockNodeWithSingleChild: Illegal write state lock: ${Thread.currentThread().id}")
            writeLock.unlock()
            prevChildLock.unlock()
            return false
        }
        if (curr.hasTwoChildren() || curr.isLeaf()) {
            writeLock.unlock()
            prevChildLock.unlock()
            println("lockNodeWithSingleChild: Curr is leaf or has 2 children, $curr: ${Thread.currentThread().id}")
            return false
        }
        val (childChildLock, childChildLockRes) = curr.lockChildRef(child)
        if (!childChildLockRes) {
            println("lockNodeWithSingleChild:Illegal child child lock: ${Thread.currentThread().id}")
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
            println("deleteLeafWithNonTransitFatherWithAction: failed to lockReadState $prev: ${Thread.currentThread().id}")
            prevStateLock.unlock()
            return false
        }
        if (!lockLeaf(prev, curr, value, action)) {
            prevStateLock.unlock()
            println("deleteLeafWithNonTransitFatherWithAction: failed to lockLeaf $prev: ${Thread.currentThread().id}")
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
            println("deleteLeafWithTransitFatherWithAction: Failed to lockChildRef on $gprev: ${Thread.currentThread().id}")
            return false
        }
        val (prevWriteStateLock, prevWriteStateLockRes) = prev.writeLockState(true)
        if (!prevWriteStateLockRes) {
            prevWriteStateLock.unlock()
            gprevChildLock.unlock()
            println("deleteLeafWithTransitFatherWithAction: Failed to lockWriteState on $prev: ${Thread.currentThread().id}")
            return false
        }
        if (child !== null) {
            val (prevChildLock, prevChildLockRes) = prev.lockChildRef(child)
            if (!prevChildLockRes) {
                prevChildLock.unlock()
                prevWriteStateLock.unlock()
                gprevChildLock.unlock()
                println("deleteLeafWithTransitFatherWithAction: Failed to lockChildRef on $prev: ${Thread.currentThread().id}")
                return false
            }
            if (!lockLeaf(prev, curr, value, action)) {
                println("deleteLeafWithTransitFatherWithAction: Failed to lock leaf on $curr, $prev: ${Thread.currentThread().id}")
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
                println("deleteLeafWithTransitFatherWithAction, child IS NULL: Failed to lock leaf on $curr, $prev: ${Thread.currentThread().id}")
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
            println("Failed to lock child value on $prev: ${Thread.currentThread().id}")
            return false
        }
        val newCurrent = if (value < prev.value) {
            prev.left
        } else {
            prev.right
        }
        if (newCurrent !== curr) {
            prevChildLock.unlock()
            println("child changed: $prev: ${Thread.currentThread().id}")
            return false
        }
        val (newCurrWriteStateLock, newCurrWriteStateLockRes) = newCurrent!!.writeLockState(false)
        if (!newCurrWriteStateLockRes) {
            newCurrWriteStateLock.unlock()
            prevChildLock.unlock()
            println("Failed to take write state lock on $newCurrent: ${Thread.currentThread().id}")
            return false
        }
        if (!curr.isLeaf()) {
            newCurrWriteStateLock.unlock()
            prevChildLock.unlock()
            println("Node $curr isn't leaf: ${Thread.currentThread().id}")
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