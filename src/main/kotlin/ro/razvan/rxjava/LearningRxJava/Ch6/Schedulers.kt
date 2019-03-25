package ro.razvan.rxjava.LearningRxJava.Ch6

import com.github.davidmoten.rx.jdbc.ConnectionProviderFromUrl
import com.github.davidmoten.rx.jdbc.Database
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.intenseCalculation
import ro.razvan.rxjava.LearningRxJava.printSeparator
import ro.razvan.rxjava.LearningRxJava.printlnFromThreadName
import java.util.concurrent.Executors

/*
Thread pools are a collection of threads.
Depending on the policy of that thread pool, threads may be persisted and maintained so they can be reused.
A queue of tasks is then executed by that thread pool.

Typically in Java you use an ExecutorService as your thread pool.
However, RxJava implements its own concurrency abstraction called Scheduler.

Many of the default Scheduler implementations can be found in the Schedulers static factory class.
For a given Observer, a Scheduler will provide a thread for its thread pool that will push the emissions.
When onComplete() is called the operation will be disposed of and the thread will be given back to the thread pool, where it may be persisted and reused by another Observer.
 */

private const val CUSTOMERS = "CUSTOMERS"

object Computation {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        This scheduler will maintain a fixed number of threads based on the processor count available to your Java session, making it appropriate for computational tasks.
        Computational tasks may utilize cores to their fullest extent.
        Therefore, there is no benefit in having more worker threads than available cores to perform such work, and the Computation scheduler will ensure that.
         */

        Observable.just("Alpha", "Beta", "Gamma")
            .subscribeOn(Schedulers.computation())
            .subscribe(::println)

        Thread.sleep(1_000)

    }

}

object IO {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        IO tasks such as reading and writing databases, web requests and disk storage are less expensive on the CPU and often have idle time waiting for the data to be sent or come back.
        This means you can create threads more liberally.
        It will maintain as many threads as there are tasks and will dynamically grow, cache, and reduce the number of threads as needed.
         */

        val connection = ConnectionProviderFromUrl("jdbc:sqlite://C:\\sqlite\\databases\\test.db").get()

        connection.createStatement().execute("DROP TABLE IF EXISTS $CUSTOMERS")
        connection.createStatement().execute("CREATE TABLE $CUSTOMERS (ID INT NOT NULL PRIMARY KEY, NAME TEXT)")

        val db = Database.from(connection)

        db.update("INSERT INTO $CUSTOMERS VALUES (1, 'Razvan'),(2, 'Stefan'),(3, 'Emma')").count().subscribe {
            println("Rows inserted: $it")
        }

        printSeparator(10)

        db.select("SELECT NAME FROM $CUSTOMERS")
            .getAs(String::class.java)
            .subscribeOn(rx.schedulers.Schedulers.io())
            .subscribe { println("$it from ${Thread.currentThread().name}") }

        Thread.sleep(1_000)

        db.close()

        /*
        As a rule of thumb, assume that each subscription will result in a new thread.
         */

    }

}

object NewThread {

    /*
    The scheduler returned by the method Schedulers.newThread() will return a Scheduler that does not pool threads at all.
    It will create a new thread for each Observer and then destroy the thread when it is done.
    Comparing this scheduler to the Schedulers.io() one this does not attempt to persist and cache threads for reuse.
     */
    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("One", "Two", "Three")
            .subscribeOn(Schedulers.newThread())
            .subscribe(::printlnFromThreadName)

        Thread.sleep(1_000)

    }

}

object Single {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        With the Schedulers.single() scheduler you can run tasks on a single thread.
        It can be helpful to isolate fragile, non-threadsafe operations to a single thread.
         */

        Observable.just("One", "Two", "Three")
            .subscribeOn(Schedulers.single())
            .map(::intenseCalculation)
            .subscribe(::printlnFromThreadName)

        Observable.range(1, 3)
            .subscribeOn(Schedulers.single())
            .map(::intenseCalculation)
            .subscribe(::printlnFromThreadName)

        Thread.sleep(20_000)

    }

}

object ExecutorService {

    /*
    You can build a scheduler off a standard Java ExecutorService.
    You may choose this in order to have more custom and fine-tuned control over your thread management policies.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        We want to create a Scheduler that uses 20 threads.
         */

        val numberOfThreads = 20

        val executor = Executors.newFixedThreadPool(numberOfThreads)

        val scheduler = Schedulers.from(executor)

        Observable.just("One", "Two", "Three")
            .subscribeOn(scheduler)
            .doFinally(executor::shutdown)
            .subscribe(::println)

        /*
        Executor will keep the program alive indefinitely, so you have to manage its disposal if its life is supposed to be finite.
        Thanks to doFinally() we can shutdown the executor when the Observer finishes to consume the emissions.
         */

    }

}

object StartShutdownSchedulers {

    /*
    Each default Scheduler is lazily instantiated when you first invoke its usage.
    You can dispose the computation(), io(), newThread(), single() and trampoline() Schedulers at any time with Scheduler.shutdown() method.
    This will stop all their threads and forbid new tasks from coming in and will throw an error if you try otherwise.
    To reinitialize the Schedulers you can call their start() method so they can accept tasks again.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val scheduler = Schedulers.single()

        Observable.just("One", "Two", "Three")
            .subscribeOn(scheduler)
            .map(::intenseCalculation)
            .subscribe(::printlnFromThreadName) { println("Error occurred on the 1st Observer: $it") }

        Observable.range(1, 3)
            .subscribeOn(scheduler)
            .map(::intenseCalculation)
            .subscribe(::printlnFromThreadName) { println("Error occurred on the 2nd Observer: $it") }

        Thread.sleep(1_000)

        scheduler.shutdown()

        Thread.sleep(1_000)

        io.reactivex.Single.just(4)
            .subscribeOn(scheduler)
            .map(::intenseCalculation)
            .subscribe(::printlnFromThreadName, ::println)

        /*
        Internal error that cannot be caught with a simple try catch statement (or by specifying the onError function while subscribing)
         */

        scheduler.start()

        io.reactivex.Single.just(5)
            .subscribeOn(scheduler)
            .map(::intenseCalculation)
            .subscribe(::printlnFromThreadName, ::println)

        Thread.sleep(5_000)

        //NOTE: Cannot shutdown() the Schedulers.newThread() scheduler

    }

}
