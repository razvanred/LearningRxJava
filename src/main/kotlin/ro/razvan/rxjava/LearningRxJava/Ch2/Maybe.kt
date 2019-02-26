package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Maybe

/*
Maybe is just like Single except that it allows no emission to occur at all.
It will only emit 0 or 1 emissions, it will call onComplete() when done.
 */

/*
MaybeObserver interface:
onSuccess(value: T) is the equivalent of the onNext() method of the Observer interface
The events defined in this interface are mutually excluded: onSuccess will be called when there is a value, onComplete will be called when there is no value and onError will be called when it occurs an error
 */

fun main() {

    val source = Maybe.just(12)

    source.subscribe({ println("Process 1: $it") }, Throwable::printStackTrace) { println("Process 1 Completed!") }

    val emptySource = Maybe.empty<Int>()

    emptySource.subscribe({ println("Process 2: $it") }, Throwable::printStackTrace) { println("Process 2 Completed!") }

    /*
    RESULT:
    Process 1: 12
    Process 2 Completed!
     */

}