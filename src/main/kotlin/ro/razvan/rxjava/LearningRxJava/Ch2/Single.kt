package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer

/*
Single<T> is an Observable that will only emit one item.
It works just like an Observable, but it is limited only to operators that make sense for a single emission.
 */

/*
It has its own SingleObserver interface as well:
onSuccess() is a combination between the methods onNext() and onComplete() of the Observer interface
 */

fun main(args: Array<String>) {

    val source = Single.just("Hello")

    //There is no Single.empty() because a Single cannot be empty
    //However there is Single.never()

    source.map(String::length)
        .subscribe(::println, Throwable::printStackTrace)

}

object FirstOperator {

    @JvmStatic
    fun main(args: Array<String>) {
        val source = Observable.empty<String>()

        source.first("Nil")
            .subscribe(Consumer(::println)) //Nil

        source.firstOrError()
            .subscribe(Consumer(::println), Consumer(Throwable::printStackTrace)) //No such element exception

        /*
        In both cases the program ends successfully
         */

    }

}