package quicksort

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Fork(1)
@Measurement(iterations = 7, batchSize = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class JmhSyncSorter {
    @Param("10000000", "40000000", "70000000", "100000000")
    private var arraySize: Int = 0
    private val synchronousSorter = SyncQuickSorter()
    private lateinit var array :Array<Int>

    @Setup(Level.Invocation)
    fun createArray() {
        val random = Random(SEED)
        array = Array(arraySize) { random.nextInt() }
    }

    @Benchmark
    fun sort() {
        synchronousSorter.sort(array)
    }
}