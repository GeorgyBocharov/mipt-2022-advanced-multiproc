package bfs

import java.time.Instant


fun main() {
    val size = 500
    val bfsSync = SyncBfs(size)
    val start = Instant.now()
    val res = bfsSync.bfs(0)
    val end = Instant.now()
    println("Time in ms = ${end.toEpochMilli() - start.toEpochMilli()}")
    println(res.size)
    println(bfsSync.allVisited())
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