package ro.razvan.rxjava.LearningRxJava.Ch2

import java.util.concurrent.Executors
import java.util.concurrent.Future

fun main() {

    val future = SquareCalculator().calculate(3)

    while (!future.isDone) {
        println("Calculating...")
        Thread.sleep(300)
    }

    println(future.get())
}

private class SquareCalculator {

    private val executor = Executors.newSingleThreadExecutor()

    fun calculate(x: Int): Future<Int> {

        return executor.submit<Int> {
            Thread.sleep(1000)
            return@submit x * x
        }

    }
}