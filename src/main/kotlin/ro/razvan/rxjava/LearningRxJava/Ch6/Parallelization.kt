package ro.razvan.rxjava.LearningRxJava.Ch6

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.intenseCalculation
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger

/*
Checking the performance by emitting 10 emissions (each one requires a different amount of time to be elaborated)
Without parallelization
 */
private fun test1() {

    Observable.range(1, 10)
        .map { intenseCalculation(it) }
        .subscribe { println("Received $it at ${LocalTime.now()}") }

    /*
    (random) OUTPUT:
    Received 1 at 23:07:40.198389200
    Received 2 at 23:07:42.142733300
    Received 3 at 23:07:44.471405900
    Received 4 at 23:07:44.767187800
    Received 5 at 23:07:47.079698100
    Received 6 at 23:07:49.883101700
    Received 7 at 23:07:50.554066
    Received 8 at 23:07:50.616107800
    Received 9 at 23:07:52.088046500
    Received 10 at 23:07:53.906666600
     */

    /*
    We will probably achieve better performance with parallelization, by processing emissions in parallel
     */

}

/*
With parallelization
 */
private fun test2() {

    Observable.range(1, 10)
        .flatMap {
            Observable.just(it)
                .subscribeOn(Schedulers.computation())
                .map { it2 -> intenseCalculation(it2) }
        }
        .subscribe { println("Received $it at ${LocalTime.now()} on ${Thread.currentThread().name}") }

    Thread.sleep(20_000)

    /*
    (random - it depends also by the CPU) OUTPUT:
    Received 4 at 23:25:00.060152700 on RxComputationThreadPool-4
    Received 3 at 23:25:01.232643100 on RxComputationThreadPool-3
    Received 8 at 23:25:01.952181600 on RxComputationThreadPool-4
    Received 2 at 23:25:02.158455400 on RxComputationThreadPool-2
    Received 1 at 23:25:02.443689300 on RxComputationThreadPool-1
    Received 5 at 23:25:03.099717800 on RxComputationThreadPool-1
    Received 7 at 23:25:03.591669800 on RxComputationThreadPool-3
    Received 6 at 23:25:03.602763400 on RxComputationThreadPool-2
    Received 9 at 23:25:04.183957100 on RxComputationThreadPool-1
    Received 10 at 23:25:04.559210100 on RxComputationThreadPool-2
     */

}

/*
Instead of creating an Observable for every single emission, if I have 8 cores it would be ideal to have 8 Observables for eight streams of calculation.
We can achieve this with GroupedObservable.
The key (with 8 cores) goes for 0 to 7.
 */

private fun test3() {

    val coreCount = Runtime.getRuntime().availableProcessors() // querying the available number of cores
    val assigner = AtomicInteger(0) // threadsafe integer with threadsafe methods

    Observable.range(1, 10)
        .groupBy { assigner.incrementAndGet() % coreCount } // eight GroupedObservables that cleanly divide the emissions into eight streams
        .flatMap {
            it.observeOn(Schedulers.io())
                .map { it2 -> intenseCalculation(it2) }
        }
        .subscribe { println("Received $it at ${LocalTime.now()} on ${Thread.currentThread().name}") }

    Thread.sleep(20_000)

    /*
    (random - it depends also by the CPU) OUTPUT:
    Received 1 at 08:54:36.965133100 on RxCachedThreadScheduler-1
    Received 4 at 08:54:37.351579400 on RxCachedThreadScheduler-4
    Received 3 at 08:54:37.356573700 on RxCachedThreadScheduler-3
    Received 5 at 08:54:37.442199600 on RxCachedThreadScheduler-1
    Received 7 at 08:54:38.431940300 on RxCachedThreadScheduler-3
    Received 2 at 08:54:38.736042900 on RxCachedThreadScheduler-2
    Received 6 at 08:54:38.741417400 on RxCachedThreadScheduler-2
    Received 10 at 08:54:39.216177500 on RxCachedThreadScheduler-2
    Received 8 at 08:54:40.251912600 on RxCachedThreadScheduler-4
    Received 9 at 08:54:40.419515500 on RxCachedThreadScheduler-1
     */

    /*
    Typically, when you have an object or state existing outside an Observable chain but is modified by the Observable chain's operations that object should by made threadsafe, especially when concurrency is involved
     */

}

fun main() {

    test1()

    printSeparator()

    test2()

    printSeparator()

    test3()

}