package com.lxy.molweightcalculator.contract

@Suppress("unused")
abstract class Operator protected constructor(val negatedOpName: String) {
    abstract fun <T : Comparable<T>> test(left: T, right: T): Boolean

    companion object {
        val EQ: Operator = object : Operator("≠") {
            override fun <T : Comparable<T>> test(left: T, right: T): Boolean {
                return left == right
            }
        }

        val NE: Operator = object : Operator("=") {
            override fun <T : Comparable<T>> test(left: T, right: T): Boolean {
                return left != right
            }
        }

        val GT: Operator = object : Operator("≤") {
            override fun <T : Comparable<T>> test(left: T, right: T): Boolean {
                return left > right
            }
        }

        val GE: Operator = object : Operator("<") {
            override fun <T : Comparable<T>> test(left: T, right: T): Boolean {
                return left >= right
            }
        }


        val LT: Operator = object : Operator("≥") {
            override fun <T : Comparable<T>> test(left: T, right: T): Boolean {
                return left < right
            }
        }


        val LE: Operator = object : Operator(" > ") {
            override fun <T : Comparable<T>> test(left: T, right: T): Boolean {
                return left <= right
            }
        }
    }
}