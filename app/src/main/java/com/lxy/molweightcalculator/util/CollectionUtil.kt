package com.lxy.molweightcalculator.util

inline fun <T> indexedIterator(
    size: Int, crossinline init: (Int) -> T
) = object : Iterator<T> {
    var index = 0

    override fun hasNext() = index < size

    override fun next() = init(index++)
}

inline fun <T> indexedCollection(
    size: Int, crossinline init: (Int) -> T
) = object : Collection<T> {
    override val size get() = size

    override fun isEmpty() = size == 0

    override fun containsAll(elements: Collection<T>) =
        error("Not yet implemented")

    override fun contains(element: T) =
        error("Not yet implemented")

    override fun iterator() = indexedIterator(size, init)
}