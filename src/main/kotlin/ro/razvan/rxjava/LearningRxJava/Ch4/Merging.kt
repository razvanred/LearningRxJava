package ro.razvan.rxjava.LearningRxJava.Ch4

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
These operators will help us to take two or more Observable<T> instances and merge them into a single Observable<T>.
The merged Observable<T> will subscribe to all of its merged sources simultaneously, making it effective for merging both finite and infinite Observables.
 */

object Merge {

    /*
    The Observable.merge() factory will take two, three or four Observable<T> sources emitting the same type T and then consolidate them into a single Observable<T>.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source1 = Observable.just(1, 2, 3, 4, 5)
        val source2 = Observable.just(6, 7, 8, 9, 10)

        Observable.merge(source1, source2)
            .subscribe(::println)

        printSeparator()

        /*
        mergeWith() is the operator version of Observable.merge()
         */

        source1.mergeWith(source2)
            .subscribe(::println)

        printSeparator()

        /*
        If the emissions are cold and on the same thread they will likely fire in order.
        You should use Observable.concat() if you explicitly want to fire elements of each Observable sequentially and keep their emissions in a sequential order.
        The order of emissions from each source Observable is maintained.
         */

        //More than 4 emissions with the factory mergeArray()
        val source3 = Observable.just(11, 12, 13)
        val source4 = Observable.just(14, 15, 16)
        val source5 = Observable.just(17, 18, 19)

        Observable.mergeArray(source1, source2, source3, source4, source5)
            .subscribe(::println)

        printSeparator()

        //You can pass multiple Observable through a list in the factory Observable.merge()
        Observable.merge(listOf(source1, source2, source3, source4, source5))
            .subscribe(::println)

        printSeparator()

        /*
        The Observable.merge() works with infinite Observables.
        Since it will subscribe to all Observables and fire their emissions as soon as they are available, you can merge multiple infinite sources into a single stream.
         */
        mergeInfiniteObservables()

    }

    private fun mergeInfiniteObservables() {

        val source1 = Observable.interval(1, TimeUnit.SECONDS)
            .map { it + 1 }
            .map { "Source 1: $it seconds" }

        val source2 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .map { (it + 1) * 300 }
            .map { "Source 2: $it milliseconds" }

        Observable.merge(listOf(source1, source2))
            .subscribe(::println)

        Thread.sleep(5000)

    }

}

object FlatMap {

    /*
    The operator flatMap() performs a dynamic Observable.merge() by taking each emission and mapping it to an Observable.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        Mapping one emission to many emissions.
         */

        val source = Observable.just("Razvan", "Rosu")

        source.flatMap { t: String -> Observable.fromIterable(t.toCharArray().toList()) }
            .subscribe(::println)

        printSeparator()

        /*
        Exercise:
        Lets take a sequence of String values (each a concatenated series of values separated by "/")
        Use flatMap() on them.
        Filter for only numeric values before converting them into Int emissions.
         */

        Observable.just("521934/2342/FOXTROT", "21962/12112/78886/TANGO", "283242/4542/WHISKEY/2348562")
            .flatMap { Observable.fromIterable(it.split("/")) }
            .filter { it.matches(Regex("[0-9]+")) }
            .map { it.toInt() }
            .subscribe(::println)

        printSeparator()

        /*
        Just like Observable.merge you can also amp emissions to infinite Observables and merge them.
        We can emit simple Int values from Observable<Int> but use flatMap() on them to drive an Observable.interval(), where each one serves as the period argument.
         */

        Observable.just<Long>(2, 3, 10, 7)
            .flatMap { item ->
                Observable.interval(item, TimeUnit.SECONDS)
                    .map { seconds ->
                        "${item}s interval: ${(seconds + 1) * item} seconds elapsed."
                    }
            }
            .subscribe(::println)

        Thread.sleep(12000)

        printSeparator()

        /*
        We can evaluate each emission within a flatMap() and figure out what kind of Observable you want to return.
         */
        Observable.just(1, 0, 3, 5)
            .flatMap {
                if (it == 0)
                    Observable.empty()
                else
                    Observable.interval(it.toLong(), TimeUnit.SECONDS)
            }
        //.subscribe(::println)

        printSeparator()

        /*
        Different and many flavors and variants of flatMap(), accepting a number of overloads.
        We can pass a second combiner argument, BiFunction<T, U, R> lambda, to associate the originally emitted T value with each flat-mapped U value and turn both into an R value.
         */

        Observable.just("Alpha", "Beta", "Gamma")
            .flatMap({ Observable.fromIterable(it.toCharArray().toList()) }) { s: String, r: Char ->
                "$s-$r"
            }
            .subscribe(::println)

        /*
        here, in the second lambda argument, the first parameter is the actual String
        The second parameter is the actual character of the String.

        OUTPUT:
        Alpha-A
        Alpha-l
        Alpha-p
        Alpha-h
        Alpha-a
        Beta-B
        Beta-e
        Beta-t
        Beta-a
        Gamma-G
        Gamma-a
        Gamma-m
        Gamma-m
        Gamma-a
         */

        printSeparator()

        /*
        The flatMapIterable() operator will help us to map each T emission into an Iterable<R> instead of an Observable<R>.
        It will then emit all the R values for each Iterable<R>.
         */

        Observable.just("Razvan", "Rosu")
            .flatMapIterable {
                it.toCharArray().toList()
            } //instead of creating the Observable that wraps the Iterator the operator will handle that for us.
            .subscribe(::println)

        printSeparator()

        /*
        flatMapSingle() will handle the Singles
         */
        Observable.just("Razvan", "Rosu")
            .flatMapSingle {
                Single.just(it.length)
            }
            .subscribe(::println)

        printSeparator()

        /*
        flatMapMaybe() will handle the Maybes
         */
        Observable.just("Razvan", "", "Rosu")
            .flatMapMaybe {
                if (it.isNotBlank())
                    Maybe.just(it.length)
                else Maybe.empty()
            }
            .subscribe(::println)

        //same output as before, even if there was an empty string in the middle of the sources

    }

}

