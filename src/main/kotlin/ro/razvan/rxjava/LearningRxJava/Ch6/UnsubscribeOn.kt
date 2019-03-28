package ro.razvan.rxjava.LearningRxJava.Ch6

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.printSeparator
import ro.razvan.rxjava.LearningRxJava.printlnFromThreadName
import java.util.concurrent.TimeUnit

/*
Disposing an Observable is an expensive operation, and should be done on a separated thread
Disposing Observable on the main thread
 */
private fun test1() {

    val disposable = Observable.interval(1, TimeUnit.SECONDS)
        .map { it + 1 }
        .doOnComplete { println("Operation completed on ${Thread.currentThread().name}") }
        .doOnDispose { println("Disposing observable on ${Thread.currentThread().name}") }
        .subscribe(::printlnFromThreadName, Throwable::printStackTrace)

    Thread.sleep(3_000)

    disposable.dispose()

}

/*
Disposing the Observable on a separated Thread
 */
private fun test2() {

    val disposable = Observable.interval(1, TimeUnit.SECONDS)
        .map { it + 1 }
        .doOnComplete { println("Operation completed on ${Thread.currentThread().name}") }
        .doOnDispose { println("Disposing observable on ${Thread.currentThread().name}") }
        .unsubscribeOn(Schedulers.io())
        .subscribe(::printlnFromThreadName, Throwable::printStackTrace)

    Thread.sleep(3_000)

    disposable.dispose()

    Thread.sleep(3_000)

}

fun main() {

    test1()

    printSeparator()

    test2()

}