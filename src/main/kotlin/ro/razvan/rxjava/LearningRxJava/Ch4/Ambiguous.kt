package ro.razvan.rxjava.LearningRxJava.Ch4

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/*
The Observable.amb() factory will accept an Iterable<Observable<T>> and emit the emissions of the first Observable that emits, while the others are disposed of.
This is helpful when you have multiple sources for the same data or events and you want the fastest one to win.
 */

fun main() {

    val source1 = Observable.interval(1, TimeUnit.SECONDS)
        .map { "Source 1 second: $it" } // this source will be ignored, even if we transform the other Observable in finite

    val source2 = Observable.interval(300, TimeUnit.MILLISECONDS)
        .map { "Source 300 milliseconds: $it" }.take(10)

    val disposable = source1.ambWith(source2).subscribe(::println)

    Thread.sleep(10_000)

    disposable.dispose()

    /*
    OUTPUT:
    Source 300 milliseconds: 0
    Source 300 milliseconds: 1
    Source 300 milliseconds: 2
    Source 300 milliseconds: 3
    Source 300 milliseconds: 4
    Source 300 milliseconds: 5
    Source 300 milliseconds: 6
    Source 300 milliseconds: 7
    Source 300 milliseconds: 8
    Source 300 milliseconds: 9
     */

}