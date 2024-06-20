package com.lxy.molweightcalculator.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

private val lookup = MethodHandles.lookup()

fun Class<*>.getPrivateMethod(
    name: String,
    vararg parameterTypes: Class<*>,
): MethodHandle {
    val method = getDeclaredMethod(name, *parameterTypes)
    method.isAccessible = true
    return lookup.unreflect(method)
}
