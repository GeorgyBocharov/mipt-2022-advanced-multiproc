package bfs

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ChildrenProviderTest {
    val size = 500
    private val provider = ChildrenProvider(size)

    @Test
    fun `getDegree return 3 for corners`() {
        val firstDown = getIndex(0, 0, 0, size)
        val secondDown = getIndex(size - 1, 0, 0, size)
        val thirdDown = getIndex(0, size - 1, 0, size)
        val fourthDown = getIndex(size - 1, size - 1, 0, size)
        val firstUp = getIndex(0, 0, size-1, size)
        val secondUp = getIndex(size-1, 0, size-1, size)
        val thirdUp = getIndex(0, size-1, size-1, size)
        val fourthUp = getIndex(size-1, size-1, size-1, size)
        val corners = setOf(firstDown, secondDown, thirdDown, fourthDown, firstUp, secondUp, thirdUp, fourthUp)
        for (cornerIndex in corners) {
            Assertions.assertThat(provider.getDegree(cornerIndex)).isEqualTo(3)
        }
    }

    private fun getIndex(x: Int, y: Int, z: Int, size: Int): Int {
        return x + y * size + size * size * z
    }
}