package ro.razvan.rxjava.LearningRxJava.Ch4

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

/*
The factory Observable.combineLatest() is similar to the zip() factory, but for every emission that fires from one of the sources, it will immediately couple up with the latest emission from every other source.
It will not queue up unpaired emissions for each source, but rather cache and pair the latest one.
 */

fun main(args: Array<String>) {

    val source1 = Observable.interval(300, TimeUnit.MILLISECONDS)
    val source2 = Observable.interval(1, TimeUnit.SECONDS)

    Observable.combineLatest(source1, source2, BiFunction<Long, Long, String> { s1, s2 ->
        "Source 1: $s1, Source 2: $s2"
    }).subscribe(::println)

    Thread.sleep(3_000)

    /*
    OUTPUT:
    Source 1: 2, Source 2: 0
    Source 1: 3, Source 2: 0
    Source 1: 4, Source 2: 0
    Source 1: 5, Source 2: 0
    Source 1: 5, Source 2: 1
    Source 1: 6, Source 2: 1
    Source 1: 7, Source 2: 1
    Source 1: 8, Source 2: 1
    Source 1: 9, Source 2: 1
    Source 1: 9, Source 2: 2
     */

}

object WithLatestFrom {

    /*
    The operator withLatestFrom() will map each T emission with the latest values from other Observables and combine them
    it will only take one emission from each of the other Observables
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source1 = Observable.interval(1, TimeUnit.SECONDS)
        val source2 = Observable.interval(300, TimeUnit.MILLISECONDS)

        source1.withLatestFrom(source2, BiFunction<Long, Long, String> { s1, s2 -> "Source 1: $s1, Source 2: $s2" })
            .subscribe(::println)

        Thread.sleep(3_000)

        /*
        OUTPUT:
        Source 1: 0, Source 2: 2
        Source 1: 1, Source 2: 5
        Source 1: 2, Source 2: 9
         */

    }

}