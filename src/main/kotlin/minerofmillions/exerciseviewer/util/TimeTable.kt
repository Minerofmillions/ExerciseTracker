package minerofmillions.exerciseviewer.util

interface TimeTable<R : Comparable<R>, C : Comparable<C>, V> {
    operator fun get(row: R, col: C): V?
}

interface MutableTimeTable<R : Comparable<R>, C : Comparable<C>, V> : TimeTable<R, C, V> {
    operator fun set(row: R, col: C, value: V)
}

private class TimeTableImpl<R : Comparable<R>, C : Comparable<C>, V>(val base: Map<R, Map<C, V>>) : TimeTable<R, C, V> {
    override fun get(row: R, col: C): V? =
        base.entries.lastOrNull { it.key <= row }?.value
            ?.entries?.lastOrNull { it.key <= col }?.value
}

private class MutableTimeTableImpl<R : Comparable<R>, C : Comparable<C>, V>(val base: MutableMap<R, MutableMap<C, V>>) :
    MutableTimeTable<R, C, V> {
    override fun get(row: R, col: C): V? =
        base.entries.lastOrNull { it.key <= row }?.value
            ?.entries?.lastOrNull { it.key <= col }?.value

    override fun set(row: R, col: C, value: V) {
        base.getOrPut(row) { mutableMapOf() }[col] = value
    }
}

fun <R : Comparable<R>, C : Comparable<C>, V> timeTableOf(vararg pairs: Pair<R, Map<C, V>>): TimeTable<R, C, V> =
    TimeTableImpl(pairs.toMap())

fun <R : Comparable<R>, C : Comparable<C>, V> mutableTimeTableOf(vararg pairs: Pair<R, MutableMap<C, V>>): MutableTimeTable<R, C, V> =
    MutableTimeTableImpl(pairs.toMap().toMutableMap())

fun <R : Comparable<R>, C : Comparable<C>, V> Map<R, Map<C, V>>.toTimeTable(): TimeTable<R, C, V> =
    TimeTableImpl(this)

fun <R : Comparable<R>, C : Comparable<C>, V> MutableMap<R, MutableMap<C, V>>.toMutableTimeTable(): MutableTimeTable<R, C, V> =
    MutableTimeTableImpl(this)
