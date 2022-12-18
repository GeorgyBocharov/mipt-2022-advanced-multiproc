package bfs

import kotlinx.coroutines.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

private val ctr = AtomicInteger(0)

fun main(): Unit = runBlocking {
    val arraySize = 500
    val syncBfs = SyncBfs(arraySize)
    val result = IntArray(arraySize * arraySize * arraySize) { -1 }
    val pool = Executors.newFixedThreadPool(4)

    for (parLim in 700 .. 1500 step 100) {
        var millis = 0L
        for (i in 1..3) {
            val asyncBfs = AsyncBfs(arraySize, parLim)
            val singleIterMillis = measureTimeMillis {
                val job = launch(pool.asCoroutineDispatcher()) {
                    asyncBfs.bfs(0, result, this)
                }
                job.join()
            }
            println("ParLim: $parLim; iter $i; Result time: $singleIterMillis")
            millis += singleIterMillis
        }
        println("ParLim: $parLim; Result time: ${millis / 3.0}")
    }
    pool.shutdown()
//    ParLim: 3000; Result time: 28479
//    ParLim: 3500; Result time: 31132
//    ParLim: 4000; Result time: 29363
//    ParLim: 4500; Result time: 29119
//    ParLim: 5000; Result time: 29256
//    if (correctDistances(result, arraySize)) {
//        println("SUCCESS")
//    } else {
//        println("FAILED")
//    }


//    val options = OptionsBuilder()
////        .include(JmhAsyncBfs::class.java.simpleName)
//        .include(JmhSyncBfs::class.java.simpleName)
//        .resultFormat(ResultFormatType.JSON)
//        .jvmArgsAppend("-XX:-BackgroundCompilation", "-XmX:5G")
//        .result("benchmark_async_vs_sync_bfs.json")
//        .output("benchmark_async_vs_sync_bfs.log")
//        .build()
//    Runner(options).run()
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

private fun prettyPrint(res: Array<Array<IntArray>>) {
    val size = res.size
    for (y in 0 until size) {
        println("(x, z), y = $y")
        for (z in size - 1 downTo 0) {
            for (x in 0 until size) {
                print("${res[x][y][z]}\t")
            }
            println()
        }
    }
}