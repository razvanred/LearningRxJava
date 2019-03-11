package ro.razvan.rxjava.LearningRxJava.Ch5

import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
Multicasting also allows us to cache values that are shared across multiple Observers.
 */

object Replaying {


    /*
    The replay() operator allows us to hold onto previous emissions within a certain scope and re-emit them when a new Observer comes in.
    It will return a ConnectableObservable that will both multicast emissions as well as emit previous emissions defined in a scope.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        test1()

        printSeparator()

        test2()

        printSeparator()

        test3()

        printSeparator()

        test4()

        printSeparator()

        test5()

        printSeparator()

        test6()

    }

    private fun test6() {

        /*
        Printing on a second Observer the last 2 emissions emitted in the last 2 seconds by the Observable
         */

        val source = Observable.interval(300, TimeUnit.MILLISECONDS)
            .map { (it + 1) * 300 }
            .replay(2, 2, TimeUnit.SECONDS)
            .autoConnect()

        val disposable1 = source.subscribe { println("Observer 1: $it") }

        Thread.sleep(2_000)

        val disposable2 = source.subscribe { println("Observer 2: $it") }

        Thread.sleep(1_000)

        /*
        OUTPUT:
        Observer 1: 300
        Observer 1: 600
        Observer 1: 900
        Observer 1: 1200
        Observer 1: 1500
        Observer 1: 1800
        Observer 2: 1500
        Observer 2: 1800
        Observer 1: 2100
        Observer 2: 2100
        Observer 1: 2400
        Observer 2: 2400
        Observer 1: 2700
        Observer 2: 2700
        Observer 1: 3000
        Observer 2: 3000
         */

        disposable1.dispose()
        disposable2.dispose()

    }

    private fun test5() {

        /*
        We can save the emissions emitted in the last 2 seconds by the Observable with a specific replay() overload
         */

        val source = Observable.interval(300, TimeUnit.MILLISECONDS)
            .map { (it + 1) * 300 }
            .replay(2, TimeUnit.SECONDS)
            .autoConnect()

        val disposable1 = source.subscribe { println("Observer 1: $it") }

        Thread.sleep(2_000)

        val disposable2 = source.subscribe { println("Observer 2: $it") }

        Thread.sleep(1_000)

        /*
        OUTPUT:
        Observer 1: 300
        Observer 1: 600
        Observer 1: 900
        Observer 1: 1200
        Observer 1: 1500
        Observer 1: 1800
        Observer 2: 900
        Observer 2: 1200
        Observer 2: 1500
        Observer 2: 1800
        Observer 1: 2100
        Observer 2: 2100
        Observer 1: 2400
        Observer 2: 2400
        Observer 1: 2700
        Observer 2: 2700
        Observer 1: 3000
        Observer 2: 3000
         */

        disposable1.dispose()
        disposable2.dispose()

    }

    private fun test4() {

        val source = Observable.just("One", "Two", "Three")
            .replay(1)
            .refCount()

        source.subscribe { println("Observer 1: $it") }

        source.subscribe { println("Observer 2: $it") }

        /*
        OUTPUT:
        Observer 1: One
        Observer 1: Two
        Observer 1: Three
        Observer 2: One
        Observer 2: Two
        Observer 2: Three

        Here the cache made by replay(1) is completely useless 'cause of refCount().
        refCount() restarts the emissions from the beginning when the first Observer finish its work.
        The cache is never used 'cause the second Observer is starting from the beginning, and another cache is built to maintain the last emission (because in this case we specified the 1 (last) emission to be maintained).
         */

    }

    private fun test3() {

        val source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
            .replay(1)
            .autoConnect()

        source.subscribe { println("Observer 1: $it") }

        source.subscribe { println("Observer 2: $it") }

        Observable.just(1).replay()
        /*
        OUTPUT:
        Observer 1: Alpha
        Observer 1: Beta
        Observer 1: Gamma
        Observer 1: Delta
        Observer 1: Epsilon
        Observer 2: Epsilon
         */

    }

    private fun test2() {

        /*
        Replay with bufferSize specify.
        Useful when handling infinite Observables or when we want to limit the number of cached emissions.
         */

        val source = Observable.interval(1, TimeUnit.SECONDS)
            .replay(2)
            .autoConnect()

        val disposable1 = source.subscribe { println("Observer 1: $it") }

        Thread.sleep(3_000)

        val disposable2 = source.subscribe { println("Observer 2: $it") }

        Thread.sleep(3_000)

        disposable1.dispose()
        disposable2.dispose()

        /*
        OUTPUT:
        Observer 1: 0
        Observer 1: 1
        Observer 1: 2
        Observer 2: 1
        Observer 2: 2
        Observer 1: 3
        Observer 2: 3
        Observer 1: 4
        Observer 2: 4
        Observer 1: 5
        Observer 2: 5
         */

    }

    private fun test1() {

        /*
        Replay with no arguments will replay all previous emissions to tardy Observers, and then emit current emissions as soon as the tardy Observer is caught up.
         */

        val seconds = Observable.interval(1, TimeUnit.SECONDS)
            .replay()
            .autoConnect()

        val disposable1 = seconds.subscribe { println("Observer 1: $it") }

        Thread.sleep(3_000)

        val disposable2 = seconds.subscribe { println("Observer 2: $it") }

        Thread.sleep(3_000)


        /*
        OUTPUT:
        Observer 1: 0
        Observer 1: 1
        Observer 1: 2
        Observer 2: 0
        Observer 2: 1
        Observer 2: 2
        Observer 1: 3
        Observer 2: 3
        Observer 1: 4
        Observer 2: 4
        Observer 1: 5
        Observer 2: 5

        After 3 seconds the Observer 2 came in and immediately received the first three emissions it missed.
        After that, it receives the same emissions as Observer 1 going forward.
        This technique could be expensive with memory, as replay() will keep caching all emissions it receives.
        You can specify a buffer size in order to limit the number of emissions that are cached by the operator.
         */

        disposable1.dispose()
        disposable2.dispose()

    }

}

object Caching {

    /*
    The operator cache() is useful when you want to cache all emissions indefinitely for the long term and do not need to control the subscription behaviour to the source with ConnectableObservable.
    It will subscribe to the source on the first downstream Observer that subscribes and hold values indefinitely.
    This makes it an unlikely candidate for infinite Observables (the amount of emissions could tax your memory).
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val cachedRollingTotals = Observable.just(6, 2, 5, 7, 1, 4, 9, 8, 3)
            .scan(0) { acc, act -> acc + act }
            .cache()

        cachedRollingTotals.subscribe(::println)

        //we can also call cacheWithInitialCapacity() to specify the number of elements to be expected in the cache.

    }

}