package ro.razvan.rxjava.LearningRxJava.Ch5

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun main() {

    /*
    For a given ConnectableObservable<T>, calling autoConnect() will return an Observable<T> that will automatically call connect() after a specified number of Observers are subscribed.
    If the number of Observers is not specified, the default value will be 1.
    This will not work well when you have an unknown number of Observers and you want all of them to receive emissions.
     */

    test1()

    printSeparator()

    test2()

    printSeparator()

    test3()

    printSeparator()

    test4()

    printSeparator()

    test5()

}

private fun test5() {

    /*
    autoConnect(0) will make the Observer emit immediately, without waiting any Observer
     */

    val source = Observable.interval(1, TimeUnit.SECONDS)
        .publish()
        .autoConnect(0)

    Thread.sleep(1_100)

    val disposable1 = source.subscribe { println("Source 1: $it") }

    Thread.sleep(1_100)

    val disposable2 = source.subscribe { println("Source 2: $it") }

    Thread.sleep(2_100)

    /*
    OUTPUT:
    Source 1: 1
    Source 1: 2
    Source 2: 2
    Source 1: 3
    Source 2: 3
     */

    disposable1.dispose()
    disposable2.dispose()

}

private fun test4() {

    val source = Observable.interval(1, TimeUnit.SECONDS)
        .publish()
        .autoConnect()

    val disposable1 = source.subscribe { println("Source 1: $it") }

    Thread.sleep(3_000)

    val disposable2 =
        source.subscribe { println("Source 2: $it") } //This Observer will miss the first 3 emissions, but then it will receive all the emissions

    Thread.sleep(3_000)

    /*
    OUTPUT:

    Source 1: 0
    Source 1: 1
    Source 1: 2
    Source 2: 2
    Source 1: 3
    Source 2: 3
    Source 1: 4
    Source 2: 4
    Source 1: 5
    Source 2: 5
     */

    disposable1.dispose()
    disposable2.dispose()

}

private fun test3() {

    /*
    Connecting another Observer after the connection of the required number of the Observers specified in the autoConnect() operator
     */

    val source = Observable.range(1, 3)
        .map { Random.nextInt(0, 100_000) }
        .publish()
        .autoConnect(2)

    source.subscribe { println("Source 1: $it") }
    source.reduce { acc, act -> acc + act }.subscribe { println("Source 2: $it") }
    source.reduce(1) { acc, act -> act * acc }.subscribe(Consumer { println("Source 3: $it") })

    /*
    (random) OUTPUT:
    Source 1: 76632
    Source 1: 16456
    Source 1: 10876
    Source 2: 103964
     */

}

private fun test2() {

    /*
    Even if the Observer finish or dispose, autoConnect() will persist its subscription to the source
     */

    val source = Observable.range(1, 3)
        .map { Random.nextInt(0, 100_000) }
        .publish()
        .autoConnect(2)

    val disposable1 = source.subscribe { println("Source 1: $it") }

    disposable1.dispose()

    val disposable2 = source.reduce { acc, act -> acc + act }.subscribe { println("Source 2: $it") }
    val disposable3 = source.reduce(1) { acc, act -> acc * act }.subscribe(Consumer { println("Source 3: $it") })

    disposable2.dispose()
    disposable3.dispose()

    /*
    (random) OUTPUT:
    Source 2: 154933

    There is no sign of the Source 1 nor of the Source 3
     */

}

private fun test1() {

    val source = Observable.range(1, 3)
        .map { Random.nextInt(0, 100_000) }
        .publish()
        .autoConnect(2)

    source.subscribe { println("Source 1: $it") }
    source.reduce { acc, act -> acc + act }
        .subscribe { println("Source 2: $it") }

}

object RefCount {

    /*
    The refCount() operator on ConnectableObservable is similar to autoConnect(1), which fires after getting one subscription.
    The difference: when it has no Observers anymore, it will dispose of itself and start over when a new one comes in.
    It does not persist the subscription to the source when it has no more Observers, and when another Observer follows, it will essentially start over.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.interval(1, TimeUnit.SECONDS)
            .publish()
            .refCount()

        source.take(5).subscribe { println("Observer 1: $it") }

        Thread.sleep(3_000)

        source.take(2).subscribe { println("Observer 2: $it") }

        Thread.sleep(3_000)

        //at this point the emissions should be finished, so the emissions for the third Observer will start over

        source.subscribe { println("Observer 3: $it") }

        Thread.sleep(3_000)

        /*
        OUTPUT:
        Observer 1: 0
        Observer 1: 1
        Observer 1: 2
        Observer 1: 3
        Observer 2: 3
        Observer 1: 4
        Observer 2: 4
        Observer 3: 0
        Observer 3: 1
        Observer 3: 2
         */

    }

    object Share {

        /*
        The share() operator is an alias for .publish().refCount().
        The result will be the same as before.
         */

        @JvmStatic
        fun main(args: Array<String>) {

            val source = Observable.interval(1, TimeUnit.SECONDS)
                .share()

            source.take(5).subscribe { println("Observer 1: $it") }

            Thread.sleep(3_000)

            source.take(2).subscribe { println("Observer 2: $it") }

            Thread.sleep(3_000)

            source.subscribe { println("Observer 3: $it") }

            Thread.sleep(3_000)

        }

    }


}