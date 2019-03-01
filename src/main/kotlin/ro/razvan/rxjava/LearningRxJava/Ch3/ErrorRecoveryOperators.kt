package ro.razvan.rxjava.LearningRxJava.Ch3

import io.reactivex.Observable
import io.reactivex.functions.BiPredicate
import io.reactivex.functions.Predicate
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
Exception can occur in your Observable chain across may operators.
The onError() event is communicated down the Observable chain to the Observer.
Sometimes we want to intercept exceptions BEFORE they get to the Observer and attempt for some recovery.
 */

fun main() {

    Observable.just(1, 2, 3, 0, 5)
        .map { 10 / it }
        .subscribe(::println, {
            println("Received error: $it")
        }) {
            println("Done!")
        }

    /*
    OUTPUT:
    10
    5
    3
    Received error: java.lang.ArithmeticException: / by zero

    NOTE: onError has been called, there were no more emission after the exception, and the program finished successfully.
     */

}

object OnErrorReturn {

    /*
    The operator onErrorReturn will allow to specify a default value used when an exception occurs
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .onErrorReturnItem(-1)
            .subscribe(::println, { println("Error received: $it") }) { println("Done!") }

        printSeparator()

        /*
        The emissions anyway will be stopped after the exception occurs using this operator.ù
        OUTPUT:
        10
        5
        3
        -1
        Done!


        NOTE: The application is finished successfully and onComplete is called (instead of onError, because they are mutually exclusive).
         */

        /*
        Same operation, but here we can produce the emission of the error through a lambda.
         */

        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .onErrorReturn { -1 }
            .subscribe(::println, { println("Error received: $it") }) { println("Done!") }

        printSeparator()

        /*
        If we want to continue to emit emissions we have to handle the exception inside the map() operator and return -1 if the exception happens
         */

        Observable.just(1, 2, 3, 0, 5)
            .map {

                try {
                    10 / it
                } catch (error: ArithmeticException) {
                    -1
                }

            }.subscribe(::println, { println("Error received: $it") }) { println("Done!") }

    }

}


object OnErrorResumeNext {

    /*
    Very similar to onErrorReturn(), the only difference is that it accepts another Observable as a parameter to emit potentially multiple values in the event of an exception.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .onErrorResumeNext(Observable.just(-1).repeat(3))
            .subscribe(::println, { println("Error caught: $it") }) { println("Done!") }

        /*
        OUTPUT:
        10
        5
        3
        -1
        -1
        -1
        Done!
         */

        printSeparator()

        /*
        We can even pass an empty Observable in order to end gracefully the emissions.
         */

        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .onErrorResumeNext(Observable.empty())
            .subscribe(::println, { println("Error caught: $it") }) { println("Done!") }

        /*
        OUTPUT:
        10
        5
        3
        Done!
         */

        printSeparator()

        /*
        Similar to onErrorReturn we can pass a lambda function (Function<Throwable,Observable<T>>) to produce dynamically the Observable from the emitted Throwable
         */

        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .onErrorResumeNext { _: Throwable -> return@onErrorResumeNext Observable.just(-1).repeat(3) }
            .subscribe(::println, { println("Error caught: $it") }) { println("Done!") }

    }

}

object Retry {

    /*
    The operator retry() has several parameters overloads. It will re-subscribe to the preceding Observable and, hopefully, not have the error again.
    If the retry() is called with no arguments it will re-subscribe an infinite number of times for each error.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        //retry the operation 2 times after the first failure
        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .retry(2)
            .subscribe(::println, { println("Error caught: $it") }) { println("Done!") }

        /*
        OUTPUT:
        10
        5
        3
        10
        5
        3
        10
        5
        3
        Error caught: java.lang.ArithmeticException: / by zero
         */

        printSeparator()

        /*
        We can also provide Predicate<Throwable> or BiPredicate<Int, Throwable> to conditionally control when retry() is attempted.
         */

        //using Predicate<Throwable>
        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .retry(Predicate { return@Predicate it !is ArithmeticException })
            .subscribe(::println, { println("Error caught: $it") }) { println("Done!") }

        printSeparator()

        //using BiPredicate<Int, Throwable>
        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .retry(BiPredicate { count, t -> return@BiPredicate count <= 2 && t is ArithmeticException })
            .subscribe(::println, { println("Error caught: $it") }) { println("Done!") }

        printSeparator()

        /*
        The retryUntil() operator will allow retries while a given BooleanSupplier lambda is false.
        There is also an advanced retryWhen() operator that supports advanced composition for tasks such as delaying retries
         */

        //retryUntil()

    }

    @Suppress("unused")
    fun retryUntil() {
        val seconds = ArrayList<Long>()

        val disposable = Observable.interval(1, TimeUnit.SECONDS)
            .subscribe({
                seconds.add(it)
            }, {
                println("Error in interval: $it")
            }) { println("Interval Observable Done!") }

        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .retryUntil {
                seconds.size >= 3
            }
            .subscribe(::println, { println("Error in the Main Observable: $it") }) { println("Main Observable Done!") }

        Thread.sleep(5000)

        disposable.dispose()

        /*
        OUTPUT: it stops retrying after 3 seconds as expected
         */
    }

    @Suppress("unused")
    fun chaos() {
        Observable.just(1, 2, 3, 0, 5)
            .map { 10 / it }
            .retry()
            .subscribe(::println) //chaotic effects, it will re-subscribe infinite times to the Observable from the beginning until it solves 10 / 0
    }

}

