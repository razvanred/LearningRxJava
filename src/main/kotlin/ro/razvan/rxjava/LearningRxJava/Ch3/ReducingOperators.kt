package ro.razvan.rxjava.LearningRxJava.Ch3

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import ro.razvan.rxjava.LearningRxJava.printSeparator

/*
These operators take a series of emissions and consolidate them into a single emission (usually emitted through a Single).
Nearly all of these operators only work on a finite Observable that calls onComplete() because typically, we can consolidate only finite datasets.
 */

object Count {

    /*
    The count() operator returns the number of the emissions of the Observable (the number is wrapped into a Single).
     */
    @JvmStatic
    fun main(args: Array<String>) {

        Observable.empty<String>()
            .count()
            .subscribe(Consumer(::println))

        /*
        For an infinite Observable consider using scan() instead
         */

    }

}

object Reduce {

    /*
    Syntactically identical to scan()
    It only emits the final accumulation when the source calls onComplete
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("Super", "Man", " 2")
            .reduce("This is ") { acc, act ->
                acc + act
            }.subscribe(Consumer(::println))

        /*
        The initial value given to reduce() should be immutable.
         */

    }

}

object All {

    /*
    With the all() operator we can verify if all the emissions qualify for a specified property.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(2, 4, 6, 8)
            .all { it % 2 == 0 }
            .subscribe(Consumer(::println))

        printSeparator()

        Observable.empty<Int>()
            .all { it % 2 == 0 }
            .subscribe(Consumer(::println)) //for the principle of vacuous truth it will print true

    }

}

object Any {

    /*
    At least one emission should meet the condition specified in the any() operator.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(1, 2, 3, 4)
            .any { it % 2 == 0 }
            .subscribe(Consumer(::println))

        printSeparator()

        Observable.empty<Int>()
            .any { it % 2 == 0 }
            .subscribe(Consumer(::println)) // for the principle of vacuous truth it will print false (at least an emission should meet the condition, but here there is no emission)

    }

}

object Contains {

    /*
    The contains operator will check whether a specific element ever emits from an Observable.
    It returns a Single<Boolean>.
    It will use the implementations of the hashCode() and equals() methods of the class.
     */

    data class Person(val name: String, val age: Int) //hashCode and equals already made thanks to data

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(
            Person("Razvan", 5),
            Person("Razvan", 50),
            Person("Razvan", 17)
        )
            .contains(Person("Razvan", 20))
            .subscribe(Consumer(::println))

    }

}