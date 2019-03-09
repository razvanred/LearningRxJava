package ro.razvan.rxjava.LearningRxJava.Ch5

import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator
import kotlin.random.Random

fun main() {

    val source = Observable.range(1, 3)
    source.subscribe { println("Source 1: $it") }
    source.subscribe { println("Source 2: $it") }

    /*
    OUTPUT:
    Source 1: 1
    Source 1: 2
    Source 1: 3
    Source 2: 1
    Source 2: 2
    Source 2: 3

    Two separate streams of data generated for two separate subscriptions.
    The emissions are regenerated for the second Observer.
     */

    printSeparator()

    /*
    Print simultaneously the emissions on both Observers
     */

    val connectableSource = Observable.range(1, 3)
        .publish()

    connectableSource.subscribe { println("Source 1: $it") }
    connectableSource.subscribe { println("Source 2: $it") }

    connectableSource.connect()

    /*
    Source 1: 1
    Source 2: 1
    Source 1: 2
    Source 2: 2
    Source 1: 3
    Source 2: 3

    The source here becomes HOT, pushing a single stream of emissions to all Observers at the same time.
    The idea of stream consolidation is known as multicasting
     */

    printSeparator()

    /*
    Two Observers: one prints random integers (obtained with the map() operator) and one finds the sum between the random integers with reduce().
     */

    exercise()

}

private fun exercise() {

    val source = Observable.range(1, 3).map { Random.nextInt(0, 100_000) }.publish()

    source.subscribe {
        println("Source 1: $it")
    }

    source.reduce { acc, act -> acc + act }
        .subscribe { println("Source 2: $it") }

    source.connect()

}

object Operators {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        Emitting the numbers 1 through 3 and map each one to a random integer between 0 and 100_000.
        The result should be different for each Observer because the number is randomly generated each time.
         */

        val source = Observable.range(1, 3)
            .map { Random.nextInt(0, 100_000) }

        source.subscribe { println("Source 1: $it") }
        source.subscribe { println("Source 2: $it") }

        /*
        Here the source will yield two separate emission generators, and each will coldly emit a separate stream for each Observer.
        Each stream also has its own separate map() instance, hence each Observer gets different random integers.
         */

        printSeparator()

        retrying()

        printSeparator()

        effectiveMulticast()

    }

    private fun effectiveMulticast() {

        val source = Observable.range(1, 3)
            .map { Random.nextInt(0, 100_000) }
            .publish()

        source.subscribe { println("Source 1: $it") }
        source.subscribe { println("Source 2: $it") }

        source.connect()

        /*
        (random) OUTPUT:
        Source 1: 99768
        Source 2: 99768
        Source 1: 45915
        Source 2: 45915
        Source 1: 21025
        Source 2: 21025

        The Observer are given the same emissions
         */

    }

    private fun retrying() {

        val source = Observable.range(1, 3)
            .publish()

        val threeRandom = source.map { Random.nextInt(0, 100_000) }

        threeRandom.subscribe { println("Source 1: $it") }
        threeRandom.subscribe { println("Source 2: $it") }

        source.connect()

        /*
        Here the Observers receive the same emissions from source, but then with the operator map() for each Observer the numbers will be picked randomly
         */

    }

}