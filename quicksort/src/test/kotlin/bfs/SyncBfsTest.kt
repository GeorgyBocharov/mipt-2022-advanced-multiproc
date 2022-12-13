package bfs

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

private const val size = 40

class SyncBfsTest {
    private val bfsSync = SyncBfs(size)

    @Test
    fun `find distances in cube array`() {
        val distances = bfsSync.bfs(0)
        testDistances(distances)
    }

    private fun testDistances(distances: IntArray) {
        for (i in distances.indices) {
            val z: Int = i / (size * size)
            val y: Int = (i % (size * size)) / size
            val x: Int = (i % (size * size)) % size
            Assertions.assertThat(distances[i]).isEqualTo(x + y + z)
        }
    }
}