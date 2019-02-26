package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

fun main() {

    /*
    Observable.range(start: Int, count: Int)
    The first argument is the starting value
    The second argument is the total count of emissions
     */
    Observable.range(0, 10)
        .subscribe(::println) //it will print 10 numbers (count), in this case from 0 to 9

    /*
    There is an equivalent Observable.rangeLong() to emit larger numbers
     */

    printSeparator()

    /*
    Observable.interval() will emit a consecutive long emission at every specified time interval.
    Here we have an Observable<Long> that emits every second
     */

    val timedSource = Observable.interval(1, TimeUnit.SECONDS)

    timedSource.subscribe { println("$it Missisipi") } // running on a separated Thread, so the main thread should be stopped in order to wait its completion

    Thread.sleep(5000)

    printSeparator()

    /*
    This method emits infinitely at the specified interval.
    However because it operates on a timer, it needs to run on a separate Thread and will run on the Computational Scheduler by default
     */

}

object ColdInterval {

    /*
    Question: Does the Observable.interval() method return a hot or a cold Observable?
    Because it is event-driven (and infinite) you may be tempted to say it is hot (wrong answer).
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.interval(1, TimeUnit.SECONDS)

        source.subscribe { println("Observer 1: $it") }

        Thread.sleep(5000)

        source.subscribe { println("Observer 2: $it") }

        Thread.sleep(5000)

        /*
        RESULT:
        Observer 1: 0
        Observer 1: 1
        Observer 1: 2
        Observer 1: 3
        Observer 1: 4
        Observer 1: 5
        Observer 2: 0
        Observer 2: 1
        Observer 1: 6
        Observer 1: 7
        Observer 2: 2
        Observer 1: 8
        Observer 2: 3
        Observer 1: 9
        Observer 2: 4
         */

    }

    /*
    Observable.interval returns a Cold Observable
    The two Observers got their own emissions, each starting at 0.
     */

}

object HotInterval {

    /*
    Here the two Observers will get the same emissions (even if the second Observer will miss the first of them in this case)
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.interval(1, TimeUnit.SECONDS)
            .publish()

        source.subscribe { println("Observer 1: $it") }
        source.connect()

        Thread.sleep(5000)

        source.subscribe { println("Observer 2: $it") }

        Thread.sleep(5000)

        /*
        RESULT:
        Observer 1: 0
        Observer 1: 1
        Observer 1: 2
        Observer 1: 3
        Observer 1: 4
        Observer 1: 5
        Observer 2: 5
        Observer 1: 6
        Observer 2: 6
        Observer 1: 7
        Observer 2: 7
        Observer 1: 8
        Observer 2: 8
        Observer 1: 9
        Observer 2: 9
         */

    }

}

object FutureSupport {

    /*
    If you have existing libraries that yield Futures you can easily turn them into Observables via Observable.from(future: Future)
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val future = SquareCalculator().calculate(3)

        Observable.fromFuture(future)
            .subscribe { println("RESULT: $it") }

    }

    private class SquareCalculator {

        private val executor = Executors.newSingleThreadExecutor()

        fun calculate(x: Int): Future<Int> {

            return executor.submit<Int> {
                Thread.sleep(2000)
                return@submit x * x
            }
        }

    }

}

object EmptyObservable {

    /*
    Empty Observable that emits nothing and calls onComplete.
    Empty Observables are common to represent empty datasets. They can also result from operators such as filter() when all emission fail to meet a condition.
    An empty observable is essentially RxJava's concept of null. It is the absence of value (technically "values").
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val empty = Observable.empty<String>()

        empty.subscribe(::println, Throwable::printStackTrace) { println("Done!") }

    }

}

object NeverObservable {

    /*
    Primarily used for testing and not that often in production.
    The difference between Observable.empty() and Observable.never() is that the second will never call onComplete(),
    forever leaving observers waiting for emissions but never actually giving any.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.never<String>()

        source.subscribe(::println, Throwable::printStackTrace) { println("Done!") }

        Thread.sleep(5000) // we have to use it because the Main thread will not wait for the Observable to emit values

    }

}

object ErrorObservable {

    /*
    This will be used only in testing too.
    This Observable will call immediately onError() with a specified exception.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.error<String>(Exception("Crash and burn!"))

        source.subscribe(::println, Throwable::printStackTrace) { println("Done!") }

    }

}

object DeferObservable {

    /*
    Observable.defer() is a factory that creates a separate state for each Observer
     */

    private const val start = 1
    private var count = 5

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.range(start, count)

        source.subscribe { println("Observer 1: $it") }

        //modify count
        count = 10

        source.subscribe { println("Observer 2: $it") }

        /*
        RESULT:
        Observer 1: 1
        Observer 1: 2
        Observer 1: 3
        Observer 1: 4
        Observer 1: 5
        Observer 2: 1
        Observer 2: 2
        Observer 2: 3
        Observer 2: 4
        Observer 2: 5
        */

        //the second Observer does not see the change

        printSeparator()

        /*
        To remedy this problem of Observable sources not capturing state changes, you can create a fresh new Observable for each subscription.
        This can be achieved using Observable.defer(), which accepts a lambda instructing how to create an Observable for every subscription.
         */

        //initializing
        count = 5

        val deferredSource = Observable.defer { Observable.range(start, count) }

        deferredSource.subscribe { println("Observer 1: $it") }

        count = 10

        deferredSource.subscribe { println("Observer 2: $it") }

    }

}

/*
If you need to perform an action or a calculation and then emit it you can use Observable.just().
But sometimes we want to do it in a lazy or deferred manner.
Also, if that procedure throws an error we want it to be emitted up the Observable chain through onError() rather than throw the error at that location.
If you try to wrap Observable.just() around 1/0 the exception will be thrown, not emitted up to the Observer
 */

object JustObservableErrorHandling {

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1 / 0).subscribe(::println, { println("error: $it") }) { println("Done!") }

        /*
        Exception in thread "main" java.lang.ArithmeticException: / by zero
        Process finished with exit code 1
         */

    }

}

/*
Here the supplier (Callable) will emit the error down the chain to the Observer where it will be handled
 */

object ReactiveErrorHandling {

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.fromCallable(Callable<Int> { 1 / 0 })
            .subscribe(::println, { println("error: $it") }) { println("Completed!") }

        /*
        RESULT:
        error: java.lang.ArithmeticException: / by zero
        Process finished with exit code 0
         */

    }

}