package minerofmillions.exerciseviewer.util

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

internal class FunctionsKtTest {

    @Test
    fun scanValues() {
        val initialMap = mapOf(
            'A' to 1,
            'B' to 2,
            'C' to 3,
            'D' to 4
        )
        val resultMap = initialMap.scanValues(0) { acc, (_, value) -> acc + value }

        assertIterableEquals(initialMap.keys, resultMap.keys)
        assertIterableEquals(listOf(1, 3, 6, 10), resultMap.values)
    }
}
