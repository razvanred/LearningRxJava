package ro.razvan.rxjava.LearningRxJava.Ch4

import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
The concatenation is similar to merging, but it will fire elements of each provided Observable sequentially and in the order specified.
It will not move on to the next Observable until the current one calls onComplete().
It is often a poor choice for infinite Observables, as an infinite Observable will indefinitely hold up the queue and forever leave subsequent Observable waiting.
 */

object Concat {

    /*
    The Observable.concat() factory is the concatenation equivalent to Observable.merge().
    It will file emissions each one sequentially and only move to the next after onComplete() is called.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.concat(Observable.just(1, 2, 3, 4), Observable.just(5, 6, 7))
            .subscribe(::println)

        printSeparator()

        Observable.just(1, 2, 3, 4).concatWith(Observable.just(5, 6, 7))
            .subscribe(::println)

        printSeparator()

        /*
        We can use the operator take() to make infinite Observables finite
         */

        val source1 = Observable.interval(1, TimeUnit.SECONDS)
            .take(2)
            .map { it + 1 }
            .map { "Source 1: $it seconds" }

        val source2 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(3)
            .map { (it + 1) * 300 }
            .map { "Source 2: $it milliseconds" }

        Observable.concat(source1, source2)
            .subscribe(::println)

        Thread.sleep(4_000)

    }

}

object ConcatMap {

    /*
    The flatMap() operator dynamically merges Observables derived off each emission.
    The concatMap() operator is the concatenation counterpart of flatMap().
    You should prefer this operator if you care about ordering and want each Observable mapped from each emission to finish before starting the next one.
    concatMap() will merge each mapped Observable sequentially and fire it one at a time. It will only move to the next Observable when the current one calls onComplete().
    If source emissions produce Observables faster than concatMap() can emit from them, those Observable will be queued.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("One", "Two", "Three")
            .concatMapIterable { it.toList() } // preferably use this operator in this particular case
            .subscribe(::println)

        printSeparator()

        /*
        concatMapEager() operator will eagerly subscribe to all Observable sources it receives and will cache the emissions until it is their turn to emit.
         */

        Observable.just("One", "Two", "Three")
            .concatMapEager { Observable.fromIterable(it.toList()) } // here there is no reason to use this operator
            .subscribe(::println)

    }

}