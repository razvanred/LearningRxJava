package ro.razvan.rxjava.LearningRxJava.Ch6

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.printSeparator
import ro.razvan.rxjava.LearningRxJava.printlnFromThreadName
import ro.razvan.rxjava.LearningRxJava.writeOnFile

/*
The subscribeOn() operator instructs the source Observable which Scheduler to emit emissions on.
If subscribeOn() is the only concurrent operation in an Observable chain, the thread from that Scheduler will work the entire Observable chain, pushing emissions from the source all the way to the final Observer.
The observeOn() will intercept emissions at that point in the Observable chain and switch them to a different Scheduler going forward.

The placement of the observeOn() operator matters!
 */

private fun test1() {

    Observable.just("HELLO/WORLD/TANGO/27A/34", "655/SUPER", "DUPER/t/GHI")
        .subscribeOn(Schedulers.io())
        .flatMap { Observable.fromIterable(it.split("/")) }
        .observeOn(Schedulers.computation())
        .filter { it.matches(Regex("[0-9]+")) }
        .map(String::toInt)
        .reduce { acc, act -> acc + act }
        .subscribe(::printlnFromThreadName)

    Thread.sleep(1_000)

}

/*
To get the data from a remote server and wait for them you should use the Schedulers.io() scheduler.
But once you got them maybe you want to do intensive computations with them, and Schedulers.io() may no longer be appropriate.
Thanks to observerOn() we can redirect the data to the Schedulers.computation() scheduler.

You can use multiple observeOn() operators to switch operators more than once.
 */
private fun test2() {

    Observable.just("HELLO/WORLD/TANGO/27A/34", "655/SUPER", "DUPER/t/GHI")
        .subscribeOn(Schedulers.io())
        .flatMap { Observable.fromIterable(it.split("/")) }
        .doOnNext { println("Split out $it on ${Thread.currentThread().name}") }
        .observeOn(Schedulers.computation())
        .filter { it.matches(Regex("[0-9]+")) }
        .map(String::toInt)
        .reduce { total, next -> total + next }
        .doOnSuccess { println("Calculated sum on ${Thread.currentThread().name}") }
        .observeOn(Schedulers.io())
        .map(Int::toString)
        .doOnSuccess { println("Writing $it to file on ${Thread.currentThread().name}") }
        .subscribe { writeOnFile(text = it, path = "C:/temp/0.txt") }

    Thread.sleep(1_000)

    /*
    OUTPUT:
    Split out HELLO on RxCachedThreadScheduler-1
    Split out WORLD on RxCachedThreadScheduler-1
    Split out TANGO on RxCachedThreadScheduler-1
    Split out 27A on RxCachedThreadScheduler-1
    Split out 34 on RxCachedThreadScheduler-1
    Split out 655 on RxCachedThreadScheduler-1
    Split out SUPER on RxCachedThreadScheduler-1
    Split out DUPER on RxCachedThreadScheduler-1
    Split out t on RxCachedThreadScheduler-1
    Split out GHI on RxCachedThreadScheduler-1
    Calculated sum on RxComputationThreadPool-2
    Writing 689 to file on RxCachedThreadScheduler-1
     */

}

fun main() {

    test1()

    printSeparator()

    test2()

}