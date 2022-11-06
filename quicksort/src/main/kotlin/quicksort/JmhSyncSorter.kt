package quicksort

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class JmhSyncSorter {
    private val synchronousSorter = SyncQuickSorter()
    private lateinit var array :Array<Int>

    @Setup(Level.Invocation)
    fun createArray() {
        val random = Random(SEED)
        array = Array(ARRAY_LENGTH) { random.nextInt() }
    }

    @Benchmark
    fun sort(blackHole: Blackhole) {
        val result = synchronousSorter.sort(array)
        blackHole.consume(result)
    }
}