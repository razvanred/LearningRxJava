package ro.razvan.rxjava.LearningRxJava.Ch3

import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
There operators will suppress emissions that fail to meet a specified criterion.
These operators work by simply not calling the onNext() function downstream for a disqualified emission.
*/

object Filter {

    @JvmStatic
    fun main(args: Array<String>) {

        println("FILTERing non-even numbers")
        Observable.just(1, 2, 3, 4, 5).filter { it % 2 == 0 }.subscribe(::println)
        printSeparator()

    }

}

object Take {

    /*

    TAKE
    it has two overloads:

    1) takes the first n emissions of the Observable (n is a positive integer)
    2) takes the first emissions emitted in a specified amount of time from beginning

     */

    @JvmStatic
    fun main(args: Array<String>) {
        println("TAKING the first 3 emissions, then the onComplete() method will be called")
        Observable.just(1, 2, 3, 4, 5).take(3).subscribe(::println)
        printSeparator()

        println("TAKING the first emissions emitted in the first 5 seconds")
        val disposable = Observable.interval(1, TimeUnit.SECONDS).take(5, TimeUnit.SECONDS).subscribe(::println)
        Thread.sleep(10_000)
        disposable.dispose()
        printSeparator()

        println("TAKING the last 3 emissions")
        Observable.just(1, 2, 3, 4, 5).takeLast(3).subscribe(::println)
        printSeparator()
    }

}

object Skip {

    /*
    The Skip Operator does the opposite work of the Take operator.
    It will ignore the specified number of emissions and then emit the ones that follow.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3, 4, 5).skip(3).subscribe(::println)
        printSeparator()

        val disposable = Observable.interval(1, TimeUnit.SECONDS).skip(3, TimeUnit.SECONDS).subscribe(::println)
        Thread.sleep(10_000)
        disposable.dispose()
        printSeparator()

        Observable.just(1, 2, 3, 4, 5).skipLast(3).subscribe(::println)
        printSeparator()

    }

}

object TakeSkipWhile {

    @JvmStatic
    fun main(args: Array<String>) {

        printSeparator()

        println("TAKE WHILE")
        Observable.just(1, 2, 3, 4, 5).takeWhile { it <= 3 }.subscribe(::println)
        printSeparator()

        println("SKIP WHILE")
        Observable.just(1, 2, 3, 4, 5).skipWhile { it <= 3 }.subscribe(::println)
        printSeparator()

    }

}

object TakeSkipUntil {

    @JvmStatic
    fun main(args: Array<String>) {

        printSeparator()

        println("TAKE UNTIL")
        var disposable = Observable.interval(1, TimeUnit.SECONDS)
            .takeUntil(Observable.interval(3, TimeUnit.SECONDS))
            .subscribe(::println)

        Thread.sleep(10_000)
        disposable.dispose()

        /*
        OUTPUT:
        TAKE UNTIL
        0
        1
        2
         */

        printSeparator()

        println("SKIP UNTIL")
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .skipUntil(Observable.interval(3, TimeUnit.SECONDS))
            .subscribe(::println)

        Thread.sleep(10_000)
        disposable.dispose()

        /*
        OUTPUT:
        SKIP UNTIL
        3
        4
        5
        6
        7
        8
        9
        10
         */

        printSeparator()

    }

}

object Distinct {

    /*
    The Distinct operator ensures that all the emissions emitted from the Observable will be different between them
     */

    @JvmStatic
    fun main(args: Array<String>) {

        printSeparator()

        Observable.just(1, 2, 3, 1, 2, 1, 1).distinct().subscribe(::println)
        printSeparator()

        //it will be printed cities with different length
        Observable.just("Susegana", "Milano", "Padova", "Venezia").distinct(String::length).subscribe(::println)
        printSeparator()

        /*
        OUTPUT:
        Susegana
        Milano
        Venezia
         */

    }

}

object DistinctUntilChanged {

    /*
    Works just like Distinct, except that here the function will discard duplicate consecutive emissions.
    Helpful way to ignore repetitions until they change.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        printSeparator()

        Observable.just(1, 1, 2, 2, 2, 3, 3, 1, 3)
            .distinctUntilChanged()
            .subscribe(::println)

        /*
        OUTPUT:
        1
        2
        3
        1
        3
         */

        printSeparator()

        Observable.just("Milano", "Padova", "Savona", "Venezia", "Torino")
            .distinctUntilChanged(String::length)
            .subscribe(::println)

        /*
        OUTPUT:
        Milano
        Venezia
        Torino
         */

        printSeparator()
    }

}

object ElementAt {

    /*
    The operator elementAt() will gather us a particular emission by its index (starting at 0).
    It returns a Maybe, specifying a default value or using the operator elementAtOrError() it will return a Single.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        printSeparator()

        Observable.just(1, 2, 3)
            .elementAt(3)
            .subscribe(::println, Throwable::printStackTrace) { println("No element emitted :(") }

        printSeparator()

    }

}