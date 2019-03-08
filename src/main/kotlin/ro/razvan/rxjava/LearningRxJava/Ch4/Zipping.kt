package ro.razvan.rxjava.LearningRxJava.Ch4

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/*
Zipping allows us to take an emission from each Observable source and combine it into a single emission.
Each Observable can emit a different type, but we can combine these different emitted types into a single emission.
 */

fun main() {

    val source1 = Observable.just("One", "Two", "Three")
    val source2 = Observable.just(1, 2, 3, 4, 5)

    source1.zipWith(source2, BiFunction<String, Int, String> { s1, s2 -> "$s1: $s2" })
        .subscribe(::println)

    /*
    OUTPUT:
    One: 1
    Two: 2
    Three: 3
     */

    /*
    If one or more sources are producing emissions faster than another zip() will queue up those rapid emissions as they wait on the slower source to provide emissions.
    If you only care about zipping the latest emission from each source rather than catching up an entire queue, you will want to use combineLatest()
     */

    printSeparator()

    val source3 = Observable.interval(1, TimeUnit.SECONDS)

    Observable.zip(source1, source3, BiFunction<String, Long, String> { s1, _ -> s1 })
        .subscribe { println("Received $it at ${LocalTime.now()}") }

    Thread.sleep(5_000)

    /*
    OUTPUT:
    Received One at 10:58:00.933134
    Received Two at 10:58:01.924621
    Received Three at 10:58:02.923791
     */

}