package quicksort

import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

const val SEED = 13
const val ARRAY_LENGTH = 1_000_000

fun main() {
    val options = OptionsBuilder()
        .include(JmhAsyncSorter::class.java.simpleName)
        .include(JmhSyncSorter::class.java.simpleName)
        .resultFormat(ResultFormatType.JSON)
        .result("benchmark_sequence.json")
        .output("benchmark_sequence.log")
        .build()
    Runner(options).run()
}