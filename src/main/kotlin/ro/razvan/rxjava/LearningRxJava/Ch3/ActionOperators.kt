package ro.razvan.rxjava.LearningRxJava.Ch3

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
Helpful operators that can assist in debugging as well as getting visibility into an Observable chain.
 */

object DoOnNextOnCompleteOnError {

    /*
    doOnNext(), doOnComplete(), doOnError()
    These three operators are like putting a mini Observer right in the middle of the Observable chain.
    These operators don't affect the operation or transform the emissions in any way.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("Hello", "World", "How", "Are", "You")
            .doOnNext { println("Processing: $it") }
            .doOnComplete { println("Source is done emitting!") }
            .doOnError { println("Reporting the issue: $it") }
            .doAfterNext { println("Processed: $it") }
            .map(String::length)
            .subscribe(
                { println("Received string length: $it") },
                { println("Error caught: $it") }) { println("Done!") }

        printSeparator()

        /*
        Simplifying with doOnEach
         */
        Observable.just("Hello", "World", "How", "Are", "You")
            .doOnEach(object : Observer<String> {

                override fun onComplete() {
                    println("Source is done emitting!")
                }

                override fun onError(e: Throwable) {
                    println("Reporting the issue: $e")
                }

                override fun onNext(t: String) {
                    println("Processing: $t")
                }

                override fun onSubscribe(d: Disposable) {
                    println("Subscribing")
                }

            })
            .doAfterNext { println("Processed: $it") }
            .map(String::length)
            .subscribe(
                { println("Received string length: $it") },
                { println("Error caught: $it") }) { println("Done!") }

        //identical result

        printSeparator()

        Observable.just("Hello", "World", "How", "Are", "You")
            .doOnEach {

                when {
                    it.isOnComplete -> println("Source is done emitting!")
                    it.isOnError -> println("Reporting the issue: ${it.error}")
                    it.isOnNext -> println("Processing: ${it.value}")
                }

            }
            .doAfterNext { println("Processed: $it") }
            .map(String::length)
            .subscribe(
                { println("Received string length: $it") },
                { println("Error caught: $it") }) { println("Done!") }

        printSeparator()

        Observable.just("Hello", "World", "How", "Are", "", "Today")
            .doOnNext { println("Processing: $it") }
            .doOnComplete { println("Source is done emitting!") }
            .doOnError { println("Source is failing") }
            .doAfterNext { println("Processed: $it") }
            .map(String::length)
            .map { 10 / it }
            .doOnError { println("Emission is failing *angry*") } //here we can deduct the error occurred in the map operator since *Source is failing* has not been printed
            .subscribe(
                { println("Received string length: $it") },
                { println("Error caught: $it") }) { println("Done!") }

    }

}

object DoOnSubscribeOnDispose {

    /*
    doOnSubscribe() and doOnDispose() methods
    The first operator will provide us the access to the Disposable in case we want to dispose() in that action.
    The second operator could fire multiple times for redundant disposal requests or not at all if it is not disposed.
    The operator doFinally() allows us to define an action (through a lambda parameter) executed after the call of onComplete() or onError() (or after the disposable is disposed)
    */

    @JvmStatic
    fun main(args: Array<String>) {

        val disposable = Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { println("Subscribing") }
            .doOnDispose { println("Disposing") }
            .doFinally { println("And finally!") }
            .subscribe(::println)

        Thread.sleep(5000)

        disposable.dispose()

        /*
        OUTPUT:
        Subscribing
        0
        1
        2
        3
        4
        Disposing
        And finally!
         */

    }

}

object DoOnSuccess {

    /*
    The operator doOnSuccess() is dedicated for the observables Maybe and Single.
    It should work as doOnNext() for Observables.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        //with Single

        Observable.just(1, 2, 3, 4)
            .count()
            .doOnSuccess { println("This will have a happy ending (spoiler: $it)") }
            .subscribe(Consumer {
                println("Received: $it")
            })

        printSeparator()

        //with Maybe

        Observable.just(1, 2, 3, 4)
            .elementAt(6)
            .doOnSuccess { println("This story will have an happy ending (spoiler: $it)") }
            .subscribe({
                println("Received: $it")
            }, Throwable::printStackTrace) { println("No emission") }

        /*
        NOTE: doOnSuccess() will always give us an emission.
        The onComplete() of the Maybe observable is not considered a success (neither a failure, otherwise it would have been called onError())
         */

    }

}

