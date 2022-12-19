package quicksort

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 7, batchSize = 1)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class JmhAsyncSorter {
    @Param("10000000", "40000000", "70000000", "100000000")
    private var arraySize: Int = 0

    private val asyncSorter =  AsyncQuickSorter(500_000)
    private lateinit var array: Array<Int>
    private lateinit var threadPool: ExecutorService

    @Setup(Level.Invocation)
    fun createArray() {
        val random = Random(SEED)
        array = Array(arraySize) { random.nextInt() }
        threadPool = Executors.newFixedThreadPool(4)
    }

    @Benchmark
    fun sort(): Unit = runBlocking {
        val job = launch(threadPool.asCoroutineDispatcher()) {
            asyncSorter.sort(array)
        }
        job.join()
    }

    @TearDown(Level.Invocation)
    fun shutdownThreadPool() {
        threadPool.shutdown()
    }
}