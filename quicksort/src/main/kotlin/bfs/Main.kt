package bfs

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

fun main(): Unit = runBlocking {
    val arraySize = 500
    val result = IntArray(arraySize * arraySize * arraySize) { -1 }
    val pool = Executors.newFixedThreadPool(4)
    val parLim = 700
    val parScanLimit = 300_000

    var millisAsync = 0L
    var millisSync = 0L
    val iterations = 10
    for (i in 1..iterations) {
        val asyncBfs = AsyncBfs(arraySize, parLim, parScanLimit)
        val syncBfs = SyncBfs(arraySize)

        val singleSync = measureTimeMillis {
            syncBfs.bfs(0, result)
        }
        if (correctDistances(result, arraySize)) {
            println("[SYNC] SUCCESS")
        } else {
            println("[SYNC] FAILED")
        }
        println("[SYNC] iter $i; time: $singleSync")

        val singleAsync = measureTimeMillis {
            val job = launch(pool.asCoroutineDispatcher()) {
                asyncBfs.bfs(0, result, this)
            }
            job.join()
        }
        if (correctDistances(result, arraySize)) {
            println("[ASYNC] SUCCESS")
        } else {
            println("[ASYNC] FAILED")
        }
        println("[ASYNC] ParLim: $parLim; iter $i; time: $singleAsync")
        millisAsync += singleAsync
        millisSync += singleSync
    }
    println("[ASYNC FINAL] ParLim: $parLim; Result time: ${millisAsync / iterations.toDouble()}")
    println("[SYNC FINAL] Result time: ${millisSync / iterations.toDouble()}")
    pool.shutdown()
}

private fun correctDistances(distances: IntArray, size: Int): Boolean {
    var res = true
    for (i in distances.indices) {
        val z: Int = i / (size * size)
        val y: Int = (i % (size * size)) / size
        val x: Int = (i % (size * size)) % size
        if (distances[i] != (x + y + z)) {
            println("ERROR: d($x, $y, $z) = ${distances[i]}")
            res = false
        }
    }
    return res
}