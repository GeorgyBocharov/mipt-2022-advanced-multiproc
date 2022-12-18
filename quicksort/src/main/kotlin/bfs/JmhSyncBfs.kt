package bfs

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Fork(0)
@Measurement(iterations = 3, batchSize = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class JmhSyncBfs {
    @Param("500")
    private var arraySize: Int = 0
    private lateinit var syncBfs: SyncBfs
    private lateinit var result : IntArray

    @Setup(Level.Invocation)
    fun initData() {
        syncBfs = SyncBfs(arraySize)
        result = IntArray(arraySize * arraySize * arraySize) { -1 }
    }

    @Benchmark
    fun calcDistances() {
        syncBfs.bfs(0, result)
    }
}