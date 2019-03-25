package ro.razvan.rxjava.LearningRxJava.Ch6

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.intenseCalculation
import ro.razvan.rxjava.LearningRxJava.printSeparator
import ro.razvan.rxjava.LearningRxJava.printlnFromThreadName
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

/*
The operator subscribeOn() will suggest to the source Observable upstream which Scheduler to use and how to execute operations on one of its threads.
If that source is not already tied to a particular Scheduler, it will use the Scheduler you specify.
It will then push emissions all the way to the final Observer using that thread (unless you add observeOn).

You can put subscribeOn() anywhere in the Observable chain, but for clarity you should place it as close to the source as possible.

Having multiple Observers to the same Observable with subscribeOn() will result in each one getting its own thread (or having them waiting for an available thread if none are available).
*/
private fun test1() {

    Observable.range(1, 3)
        .subscribeOn(Schedulers.computation())
        .filter { it < 3 }
        .map(::intenseCalculation)
        .subscribe(::printlnFromThreadName)

    Observable.range(1, 3)
        .filter { it < 3 }
        .subscribeOn(Schedulers.computation())
        .map(::intenseCalculation)
        .subscribe(::printlnFromThreadName)

    Observable.range(1, 3)
        .filter { it < 3 }
        .map(::intenseCalculation)
        .subscribeOn(Schedulers.computation())
        .subscribe(::printlnFromThreadName)

    Thread.sleep(10_000)

    //same result for all of the three Observers

    /*
    (random) OUTPUT:
    1 from RxComputationThreadPool-1
    1 from RxComputationThreadPool-3
    1 from RxComputationThreadPool-2
    2 from RxComputationThreadPool-1
    2 from RxComputationThreadPool-2
    2 from RxComputationThreadPool-3
     */

}

/*
If we want only one thread to serve both Observables we can multicast this operation.
Make sure subscribeOn() is before the multicast operators.
 */
private fun test2() {

    val lengths = Observable.just("One", "Two", "Three")
        .subscribeOn(Schedulers.computation())
        .map(::intenseCalculation)
        .map(String::length)
        .publish()
        .autoConnect(2)

    lengths.subscribe(::printlnFromThreadName)

    lengths.subscribe(::printlnFromThreadName)

    Thread.sleep(10_000)

    /*
    (random) OUTPUT:
    3 from RxComputationThreadPool-4
    3 from RxComputationThreadPool-4
    3 from RxComputationThreadPool-4
    3 from RxComputationThreadPool-4
    5 from RxComputationThreadPool-4
    5 from RxComputationThreadPool-4
     */

}

/*
For factories such as Observable.fromCallable and Observable.defer, the initialization of these sources will also run on the specified Scheduler when using subscribeOn().
 */
private fun test3() {

    fun getResponse(path: String): String {
        return try {
            Scanner(URL(path).openStream(), Charsets.UTF_8).useDelimiter("\\A").next()
        } catch (exc: Exception) {
            exc.message ?: ""
        }
    }

    Observable.fromCallable { getResponse("https://my-json-server.typicode.com/typicode/demo/posts") }
        .subscribeOn(Schedulers.io())
        .subscribe(::println)

    Thread.sleep(10_000)

    /*
    In this way the main thread is not blocked to wait a URL response

    OUTPUT:
    [
      {
        "id": 1,
        "title": "Post 1"
      },
      {
        "id": 2,
        "title": "Post 2"
      },
      {
        "id": 3,
        "title": "Post 3"
      }
    ]
     */

}

/*
subscribeOn() will have no practical effect with certain sources (and will keep a worker thread unnecessarily on stand-by until that operation terminates).
This might be because these Observables already use a specific Scheduler, and if you want to change it, you can provide a Scheduler as an argument.
 */
private fun test4() {

    fun f1() {
        val disposable = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe(::printlnFromThreadName)

        Thread.sleep(3_100)

        /*
        OUTPUT:
        0 from RxComputationThreadPool-1
        1 from RxComputationThreadPool-1
        2 from RxComputationThreadPool-1
         */

        disposable.dispose()
    }

    fun f2() {
        val disposable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
            .subscribe(::printlnFromThreadName)

        Thread.sleep(3_100)

        /*
        OUTPUT:
        0 from RxCachedThreadScheduler-1
        1 from RxCachedThreadScheduler-1
        2 from RxCachedThreadScheduler-1
         */

        disposable.dispose()
    }

    fun f3() {
        Observable.range(1, 3)
            .subscribeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.io())
            .subscribe(::printlnFromThreadName)

        /*
        OUTPUT:
        1 from RxNewThreadScheduler-1
        2 from RxNewThreadScheduler-1
        3 from RxNewThreadScheduler-1
         */
    }

    //Demonstration
    f1()

    printSeparator(5)

    //Fixing the issue by specifying the desired Scheduler as a parameter (if allowed by the specific Observable)
    f2()

    printSeparator(5)

    /*
    If you have multiple subscribeOn() calls on a given Observable chain, the one closest to the source will win and cause any subsequent ones to have no practical effect (other than unnecessary resource usage).
     */
    f3()

}

fun main() {

    test1()

    printSeparator()

    test2()

    printSeparator()

    test3()

    printSeparator()

    test4()

}