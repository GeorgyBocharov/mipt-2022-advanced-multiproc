package bfs

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Fork(0)
@Measurement(iterations = 3, batchSize = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class JmhAsyncBfs {
    @Param("400", "500", "600")
    private var arraySize: Int = 0
    private lateinit var asyncBfs: AsyncBfs
    private lateinit var result: IntArray
    private lateinit var threadPool: ExecutorService

    @Setup(Level.Invocation)
    fun initData() {
        asyncBfs = AsyncBfs(arraySize, 3000)
        result = IntArray(arraySize * arraySize * arraySize) { -1 }
        threadPool = Executors.newFixedThreadPool(4)
    }

    @Benchmark
    fun calcDistances():Unit = runBlocking {
        val job = launch {
            asyncBfs.bfs(0, result, this)
        }
        job.join()
    }
}