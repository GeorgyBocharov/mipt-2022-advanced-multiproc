package bst

import bst.tree.Bst
import bst.tree.ParallelBst
import bst.tree.SeqBst
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.random.Random


fun main(): Unit = runBlocking {
    val maxThreads = 4
    val runWithCheck = false
    val operationExecutor = OperationExecutor()


    for (x in arrayOf(0)) {
        for (t in 1..maxThreads) {
            val k = IntArray(10) { i -> i + 1 }
            val resultList = prepopulate(k, Random(13))
            val parBst = ParallelBst()
            val operations = LinkedBlockingQueue<Operation>()
            val executor = Executors.newFixedThreadPool(t)
            var totalOperations = 0L
            var maxTime = 0L
            val list = ArrayList<Deferred<Pair<Long,Long>>>(t)
            for (i in 1..t) {
                list.add(async(executor.asCoroutineDispatcher()) {
                    process(resultList, resultList.size, x, parBst, runWithCheck, operations)
                })
            }
            for (e in list) {
                val (ops, time) = e.await()
                totalOperations += ops
                maxTime = if (time > maxTime) {
                    time
                } else {
                    maxTime
                }
            }
            executor.shutdown()
            println("For x = $x, threadsNum = $t, op/ms = ${totalOperations * 1.0 / maxTime}")
            if (runWithCheck) {
                val finalOperations = ArrayList<Operation>(operations)
                val seqBst = SeqBst()
                operationExecutor.execute(seqBst, finalOperations)
                if (!parBst.isEqualToSeqBst(seqBst)) {
                    println("CHECK FAILED!")
                }
            }

        }
    }
}

fun process(
    resultList: IntArray,
    elementsNumber: Int,
    x: Int,
    tree: Bst,
    runWithCheck: Boolean,
    operationsQueue: LinkedBlockingQueue<Operation>
): Pair<Long, Long> {
    println("Running in thread ${Thread.currentThread().id}")
    val random = Random(13)
    var counter = 0L
    val end = System.currentTimeMillis() + 5_000
    while (System.currentTimeMillis() < end) {
        val key = resultList[random.nextInt(elementsNumber)]
        val p = random.nextInt(101)
        counter++
        if (p < x) {
            if (runWithCheck)
                operationsQueue.add(Operation(key, OperationType.INSERT))
            tree.insert(key)
        } else if (p < 2 * x) {
            if (runWithCheck)
                operationsQueue.add(Operation(key, OperationType.REMOVE))
            tree.remove(key)
        } else {
            if (runWithCheck)
                operationsQueue.add(Operation(key, OperationType.CONTAINS))
            tree.contains(key)
        }
    }
    return Pair(counter, System.currentTimeMillis() - end + 5_000)
}


fun prepopulate(array: IntArray, random: Random): IntArray {
    val res = ArrayList<Int>()
    for (elem in array) {
        if (random.nextFloat() >= 0.5) {
            res.add(elem)
        }
    }
    return res.toIntArray()
}