package ro.razvan.rxjava.LearningRxJava.Ch4

import io.reactivex.Observable
import io.reactivex.observables.GroupedObservable
import ro.razvan.rxjava.LearningRxJava.printSeparator

/*
The groupBy() operator will group emissions by a specified key into separate Observables.
The operator accepts a lambda mapping each emission to a key. It will then return an Observable<GroupedObservable<K, T>> which emits a special type of Observable called GroupedObservable.
GroupedObservable is just like any other Observable, but it has the key K value accessible as a property. It will emit T emissions that are mapped for that given key.
 */

fun main() {

    val source = Observable.just("One", "Two", "Three")
    val byLengths = source.groupBy(String::length)

    byLengths.flatMapSingle(GroupedObservable<Int, String>::toList)
        .subscribe(::println)

    /*
    OUTPUT:
    [One, Two]
    [Three]
     */

    printSeparator()

    source.groupBy(String::length)
        .flatMapSingle { groupedObservable: GroupedObservable<Int, String> ->
            groupedObservable.reduce("") { acc: String, act: String -> if (acc.isEmpty()) act else "$acc, $act" }
                .map { "${groupedObservable.key}: $it" }
        }
        .subscribe(::println)

    /*
    OUTPUT:
    3: One, Two
    5: Three
     */

    /*
    GroupedObservables are a weird combination of hot and cold Observable:
    - they are not cold in that they will not replay missed emissions to a second Observer
    - but, they will cache emissions and flush them to the first Observer, ensuring non are missed
    If you need to replay emissions collect them into a list (just like we have done above) and perform the operations against the list.
     */

}