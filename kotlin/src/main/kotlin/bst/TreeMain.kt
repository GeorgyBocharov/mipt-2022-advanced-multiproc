package bst

import bst.tree.Bst
import bst.tree.ParallelBst
import bst.tree.SeqBst
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


val random = Random(13)

val operationsCounter = AtomicInteger(0)

fun main() {
    val maxThreads = 4
    val executor = Executors.newFixedThreadPool(maxThreads)
    val runWithCheck = true
    val operationExecutor = OperationExecutor()


    for (x in arrayOf(50)) {
        for (t in 1..maxThreads) {
            val k = IntArray(10) { i -> i + 1 }
            val resultList = prepopulate(k)
            val parBst = ParallelBst()
            operationsCounter.set(0)
            val operations = LinkedBlockingQueue<Operation>()
            val futures: ArrayList<CompletableFuture<Void>> = ArrayList()
            val end = System.currentTimeMillis() + 5000
            for (i in 1..t) {
                val completableFuture = CompletableFuture.runAsync(
                    { process(resultList, resultList.size, x, parBst, end, runWithCheck, operations) }, executor)
                futures.add(completableFuture)
            }
            for (f in futures) {
                f.get()
            }
            println("For x = $x, threadsNum = $t, op/ms = ${operationsCounter.get() / 5000.0}")

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
    executor.shutdown()
}

fun process(resultList: IntArray,
         elementsNumber: Int,
         x: Int,
         tree: Bst,
         end: Long,
         runWithCheck: Boolean,
         operationsQueue: LinkedBlockingQueue<Operation>) {
    while (System.currentTimeMillis() < end) {
        val key = resultList[random.nextInt(elementsNumber)]
        val p = random.nextInt(101)
        operationsCounter.getAndIncrement()
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
}


fun prepopulate(array: IntArray): IntArray {
    val res = ArrayList<Int>()
    for (elem in array) {
        if (random.nextFloat() >= 0.5) {
            res.add(elem)
        }
    }
    return res.toIntArray()
}