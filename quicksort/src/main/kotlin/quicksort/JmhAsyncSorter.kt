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
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class JmhAsyncSorter {
    private val sorter = AsyncQuickSorter()
    private lateinit var array :Array<Int>
    private lateinit var threadPool: ExecutorService

    @Setup(Level.Invocation)
    fun createArray() {
        val random = Random(SEED)
        array = Array(ARRAY_LENGTH) { random.nextInt() }
        threadPool = Executors.newFixedThreadPool(4)
    }

    @Benchmark
    fun sort(): Unit = runBlocking {
        val job = launch(threadPool.asCoroutineDispatcher()) {
            sorter.sort(array, 0, array.size - 1)
        }
        job.join()
    }

    @TearDown(Level.Invocation)
    fun shutdownThreadPool() {
        threadPool.shutdown()
    }
}