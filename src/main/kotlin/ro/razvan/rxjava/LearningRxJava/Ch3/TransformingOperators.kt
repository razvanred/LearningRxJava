package ro.razvan.rxjava.LearningRxJava.Ch3

import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

object Map {

    /*
    The map() operator will transform a T emission into an R emission using the provided Function<T, R> lambda.
    One-to-one conversion for each emission.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        printSeparator()

        Observable.just("Hello", "World")
            .map(String::length)
            .subscribe(::println)

        printSeparator()
    }

}

object Cast {

    /*
    The cast() operator casts each emission to a given class
     */

    interface Super

    class Hero : Super {
        fun fly() {
            println("flying")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {

        val heroes: List<Super> = listOf(Hero(), Hero(), Hero())

        Observable.fromIterable(heroes)
            .cast(Hero::class.java)
            .subscribe { it.fly() }

    }

}

object StartWith {

    /*
    The startWith operator allows us to insert a T emission that precedes all the other emissions.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3)
            .startWith(4)
            .subscribe(::println)

        printSeparator()

        Observable.just(1, 2, 3)
            .startWithArray(4, 5, 6)
            .subscribe(::println)

    }

    /*
    If we want an entire emissions of Observable to precede emissions of another Observable we will want to use Observable.concat() or concatWith()
     */

}

object DefaultIfEmpty {

    /*
    If we want to resort to a single emission if a given Observable comes out empty, we can use defaultIfEmpty().
    For a given Observable<T> we can specify a default T emission if no emissions occur when onComplete() is called.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.empty<String>()
            .defaultIfEmpty("No emission")
            .subscribe(::println)

        printSeparator()

    }

}

object SwitchIfEmpty {

    /*
    The operator switchIfEmpty() specifies a different Observable to emit values from if the source Observable is empty.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.empty<String>()
            .switchIfEmpty(Observable.just("Milano", "Roma", "Napoli"))
            .subscribe(::println)

    }

}


object Sorted {

    /*
    If we have a finite Observable<T> emitting items that implement Comparable<T>, we can use sorted() to sort emissions.
     */

    data class Person(val name: String, val age: Int) : Comparable<Person> {

        override fun compareTo(other: Person): Int {
            return age.compareTo(other.age)
        }

    }

    class NameComparator : Comparator<Person> {

        override fun compare(o1: Person?, o2: Person?): Int {
            return o1?.name?.compareTo(o2?.name ?: "") ?: 0
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(
            Person("Razvan", 20),
            Person("Stefan", 15),
            Person("Emma", 3)
        )
            .sorted() //sorted by age
            .subscribe(::println)

        printSeparator()

        Observable.just(
            Person("Razvan", 20),
            Person("Stefan", 15),
            Person("Emma", 3)
        )
            .sorted(NameComparator())
            .subscribe(::println)

        printSeparator()

        Observable.just(1, 2, 3, 4)
            .sorted(Comparator.reverseOrder())
            .subscribe(::println)

        printSeparator()

        Observable.just(
            Person("Alvise", 20),
            Person("Francesco", 26),
            Person("Teodora", 44)
        )
            .sorted { o1, o2 -> o1.name.length.compareTo(o2.name.length) }
            .subscribe(::println)

    }

}

object Delay {

    /*
    The operator delay() delays the emission of each element in the Observable.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("Razvan", "Stefan", "Emma")
            .delay(2, TimeUnit.SECONDS)
            .subscribe(::println)

        Thread.sleep(10_000)

        printSeparator()

        Observable.create<Int> {
            try {

                it.onNext(1)
                it.onNext(2)
                it.onNext(1 / 0)
                it.onNext(3)

                it.onComplete()

            } catch (error: Throwable) {
                it.onError(error)
            }
        }
            .delay(2, TimeUnit.SECONDS, true) // by default the error isn't delayed
            .subscribe(::println, { println("Error occurred") }) { println("Done!") }

        Thread.sleep(10_000)

    }

}

object Repeat {

    /*
    The operator repeat() will repeat subscription upstream after onComplete() a specified number of times.
    The onComplete() method is called once, at the end of the repetitions.
     */

    var counter = 0

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3, 4)
            .repeat(3) //it will repeat infite times if the number is not given
            .subscribe(::println, Throwable::printStackTrace) { println("Completed!") }

        printSeparator()

        Observable.just(1, 2, 3, 4)
            .repeatUntil { counter >= 10 }
            .subscribe({
                counter++
                println(it)
            }, Throwable::printStackTrace) { println("Completed!") }

    }

}

object Scan {

    /*
    The operator scan() is a rolling aggregator.
    For every emission you add it to an accumulation. Then, it will emit each incremental accumulation.
    DO NOT confuse it with the reduce() operator.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3, 4)
            .scan { acc, act -> acc + act }
            .subscribe(::println)

        /*
        OUTPUT:
        1
        3
        6
        10
         */

        printSeparator()

        Observable.just(1, 2, 3, 4)
            .reduce { acc, act -> acc + act }
            .subscribe(::println)

        /*
        OUTPUT: 10
         */
    }

}