package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Completable

/*
Completable is simply concerned with an action being executed, but it does not receive any emission.
It doesn't have onNext or onSuccess to receive emissions, but it does have onError() and onComplete()
 */

/*
It can be constructed by calling Completable.complete() (it will call directly onComplete() without doing anything else) or Completable.fromRunnable() (it will execute the specified action before calling onComplete())
 */

fun runProcess() {
    println("Process")
}

fun main() {

    Completable.fromRunnable { runProcess() }
        .subscribe({ println("Done") }) { println("Error!") }

    /*
    OUTPUT:
    Process
    Done
     */

}