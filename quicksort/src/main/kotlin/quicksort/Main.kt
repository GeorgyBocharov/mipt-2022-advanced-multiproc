package quicksort

import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

const val SEED = 13
//const val ARRAY_LENGTH = 100_000_000

fun main() {
    val options = OptionsBuilder()
        .include(JmhAsyncSorter::class.java.simpleName)
        .include(JmhSyncSorter::class.java.simpleName)
        .resultFormat(ResultFormatType.JSON)
        .jvmArgsAppend("-XX:-BackgroundCompilation")
        .result("benchmark_async_vs_sync_quicksort_diff_size.json")
        .output("benchmark_async_vs_sync_quicksort_diff_size.log")
        .build()
    Runner(options).run()
}