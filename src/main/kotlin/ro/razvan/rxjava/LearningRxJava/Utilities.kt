package ro.razvan.rxjava.LearningRxJava

import kotlin.random.Random

fun printSeparator(length: Int) {
    for (i in 0 until length)
        print("-")
    print("\n")
}

fun printlnFromThreadName(any: Any?) {
    println("$any from ${Thread.currentThread().name}")
}

fun javaSleep(millis: Long) {
    Thread.sleep(millis)
}

fun <T> intenseCalculation(value: T): T {
    return intenseCalculation(value, 3_000)
}

fun <T> intenseCalculation(value: T, bound: Long): T {
    Thread.sleep(Random.nextLong(bound))
    return value
}

fun printSeparator() {
    printSeparator(10)
}

fun println(message: Any?) {
    kotlin.io.println(message)
}