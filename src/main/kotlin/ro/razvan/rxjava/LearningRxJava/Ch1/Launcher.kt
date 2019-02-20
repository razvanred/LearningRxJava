package ro.razvan.rxjava.LearningRxJava.Ch1

import io.reactivex.Observable
import io.reactivex.Observer
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

fun main() {

    val myStrings: Observable<String> = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")

    /*
    An Observable can push data or events from virtually any source
    In this case with the method just we are emitting a fixed set of items
    To make this observable actually push the five strings (which are now called emissions) we need an Observer to subscribe to it and receive the items.
     */

    myStrings.subscribe {
        println(it)
    }

    printSeparator()

    /*
    We can also use several operators between Observable and Observer to transform or manipulate each pushed item in some way
    Each operator returns a new Observable derived-off the previous one but reflects the transformation
    In the example below with the map operator we are printing the length of each string
     */

    myStrings.map { it.length }.subscribe {
        println(it)
    }

    /*
    TODO understand the difference between Java Streams/Kotlin sequences and Observables
    In the example below I push a consecutive Long at each specified time interval
    This Long emission is not only data, but also an event
    I had to put to sleep the main thread in order to fire all the emissions on the computation thread
     */

    printSeparator()

    val secondIntervals = Observable.interval(1, TimeUnit.SECONDS)
    secondIntervals.subscribe {
        println(it)
    }

    Thread.sleep(5000)


}