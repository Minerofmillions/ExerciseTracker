package minerofmillions.exerciseviewer.util

interface TimeMap<K : Comparable<K>, V> : Map<K, V> {
    override fun get(key: K): V? = entries.lastOrNull { it.key <= key }?.value
}

interface MutableTimeMap<K : Comparable<K>, V> : TimeMap<K, V>, MutableMap<K, V> {
    override fun get(key: K): V? = entries.lastOrNull { it.key <= key }?.value
}

private class TimeMapImpl<K : Comparable<K>, V>(private val base: Map<K, V>) : TimeMap<K, V> {
    override val entries: Set<Map.Entry<K, V>>
        get() = base.entries
    override val keys: Set<K>
        get() = base.keys
    override val size: Int
        get() = base.size
    override val values: Collection<V>
        get() = base.values

    override fun containsKey(key: K): Boolean = keys.any { it <= key }

    override fun containsValue(value: V): Boolean = base.containsValue(value)

    override fun isEmpty(): Boolean = base.isEmpty()

    override fun toString(): String = base.toString()
}

private class MutableTimeMapImpl<K : Comparable<K>, V>(private val base: MutableMap<K, V>) : MutableTimeMap<K, V> {
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = base.entries
    override val keys: MutableSet<K>
        get() = base.keys
    override val size: Int
        get() = base.size
    override val values: MutableCollection<V>
        get() = base.values

    override fun containsKey(key: K): Boolean = keys.any { it <= key }

    override fun containsValue(value: V): Boolean = base.containsValue(value)

    override fun isEmpty(): Boolean = base.isEmpty()

    override fun clear() = base.clear()

    override fun put(key: K, value: V): V? = base.put(key, value)

    override fun putAll(from: Map<out K, V>) = base.putAll(from)

    override fun remove(key: K): V? = base.remove(key)

    override fun toString(): String = base.toString()
}

fun <K : Comparable<K>, V> timeMapOf(vararg entries: Pair<K, V>): TimeMap<K, V> = TimeMapImpl(entries.toMap())
fun <K : Comparable<K>, V> Map<K, V>.toTimeMap(): TimeMap<K, V> = TimeMapImpl(this)

fun <K : Comparable<K>, V> mutableTimeMapOf(vararg entries: Pair<K, V>): MutableTimeMap<K, V> =
    MutableTimeMapImpl(entries.toMap().toMutableMap())

fun <K : Comparable<K>, V> Map<K, V>.toMutableTimeMap(): MutableTimeMap<K, V> = MutableTimeMapImpl(toMutableMap())
