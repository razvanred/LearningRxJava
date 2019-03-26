package ro.razvan.rxjava.LearningRxJava

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
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

fun writeOnFile(text: String, path: String) {

    var writer: BufferedWriter? = null

    try {

        writer = BufferedWriter(FileWriter(File(path)))
        writer.append(text)

    } catch (exc: Exception) {

        exc.printStackTrace()

    } finally {

        writer?.close()

    }

}