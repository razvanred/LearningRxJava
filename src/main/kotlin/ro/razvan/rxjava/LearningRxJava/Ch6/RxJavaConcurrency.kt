package ro.razvan.rxjava.LearningRxJava.Ch6

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/*
You have to complete different tasks. You would be faster if you called a friend to help you.
You and your friend are threads. You do work together. Collectively, you both are a thread pool ready to execute tasks.
The tasks are queued for the thread pool, which you can execute two at a time.
If you have more friends, your thread pool will have more bandwidth to take on more tasks concurrently.
Threads are expensive to create, maintain and destroy, and there is a diminishing return in performance as you create them excessively.
That is why it is better to have a thread pool to reuse threads and have them work a queue of tasks.
 */

fun main() {

    withoutConcurrency()

    printSeparator()

    withConcurrency()

    printSeparator()

    concurrencyWithOperators()

}

private fun concurrencyWithOperators() {

    /*
    The default operators (at least) can work with Observables on different threads safely.
    Even operators and factories that combine multiple Observables (like merge and zip) will safely combine the emissions pushed by different threads.

    Zipping two Observables that emit on two separate computation threads.
     */

    val source1 = Observable.just("One", "Two", "Three", "Four", "Five", "Six")
        .subscribeOn(Schedulers.computation())
        .map { intenseCalculation(it) }

    val source2 = Observable.range(1, 6)
        .subscribeOn(Schedulers.computation())
        .map { intenseCalculation(it) }

    Observable.zip(source1, source2, BiFunction<String, Int, String> { s1, s2 -> "$s1 - $s2" }).subscribe(::println)

    Thread.sleep(20_000)

    /*
    OUTPUT:
    One - 1
    Two - 2
    Three - 3
    Four - 4
    Five - 5
    Six - 6
     */

}

private fun withConcurrency() {

    /*
    Using the operator subscribeOn() we can suggest to the source to fire the emissions on a specified Scheduler.
    In this case, Schedulers.computational() pools a fixed number of threads appropriate for computational operations.
    It will provide a thread to push emissions for each Observer.
     */

    Observable.just("One", "Two", "Three", "Four", "Five", "Six")
        .subscribeOn(Schedulers.computation())
        .map { intenseCalculation(it) }
        .subscribe(::println)

    Observable.range(1, 6)
        .subscribeOn(Schedulers.computation())
        .map { intenseCalculation(it) }
        .subscribe(::println)

    Thread.sleep(20_000)

    /*
    (random) OUTPUT:
    1
    One
    2
    Two
    Three
    3
    Four
    4
    Five
    5
    Six
    6

    Both operations are firing simultaneously now, allowing the program to finish much more quickly.
     */

}

private fun withoutConcurrency() {

    /*
    By default Observables execute work on the immediate thread, which is the thread that declared the Observer and subscribed it.
     */

    Observable.just("One", "Two", "Three", "Four", "Five", "Six")
        .map { intenseCalculation(it) }
        .subscribe(::println)

    Observable.range(1, 6)
        .map { intenseCalculation(it) }
        .subscribe(::println)

    /*
    OUTPUT:
    One
    Two
    Three
    Four
    Five
    Six
    1
    2
    3
    4
    5
    6

    Note that the first Observable has to fire onComplete() before firing the second Observable emissions.
    If we fire both Observables at the same time rather than waiting for one to complete before starting the other, we could get this operation done much more quickly.
     */

}

private fun <T> intenseCalculation(value: T): T {
    Thread.sleep(Random.nextLong(3_000))
    return value
}

object KeepApplicationAliveWithSleep {

    /*
    Technically the application will not last forever with this method
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.interval(1, TimeUnit.SECONDS)
            .map { intenseCalculation(it) }
            .subscribe(::println)

        Thread.sleep(Long.MAX_VALUE)

    }

}

object KeepApplicationAliveWithOperators {

    /*
    You can use blocking operators to stop the declaring thread and wait for emissions.
    This operators can attract anti-patterns if used improperly in production.
    Keeping an app alive based on the life cycle of a finite Observable subscription is a valid case to use a blocking operator.

    The operator blockingSubscribe() will stop and wait for onComplete() to be called before the main thread is allowed to proceed and exit the application.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source1 = Observable.just("One", "Two", "Three")
            .subscribeOn(Schedulers.computation())
            .map { intenseCalculation(it) }

        val source2 = Observable.range(1, 3)
            .subscribeOn(Schedulers.computation())
            .map { intenseCalculation(it) }

        Observable.zip(source1, source2, BiFunction<String, Int, String> { s1, s2 -> "$s1 - $s2" })
            .blockingSubscribe(::println)

    }

}
