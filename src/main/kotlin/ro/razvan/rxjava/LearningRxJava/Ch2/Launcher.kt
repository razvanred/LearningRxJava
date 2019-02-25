package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import ro.razvan.rxjava.LearningRxJava.printSeparator

fun main() {

    /*
    At the highest level an Observable works by passing three types of events:
    1. The onNext() method passes each item one at a time from the source Observable all the way down to the Observer
    2. The onComplete() method communicates a completion event all the way down to the Observer, indicating that no more onNext() calls will occur
    3. The onError() method communicates an error up the chain to the Observer, where typically the Observer defines how to handle it

    This three events are abstract methods in the Observer type
     */

    val source: Observable<String> = Observable.create { emitter: ObservableEmitter<String> ->
        emitter.onNext("Alpha") //passing the string to the Observer
        emitter.onNext("Beta")
        emitter.onNext("Gamma")
        emitter.onNext("Delta")
        emitter.onNext("Epsilon")
        emitter.onComplete()
    }

    source.subscribe { println("RECEIVED: $it") }

    printSeparator()

    /*
    NOTE: The Observable contract dictates that emissions must be passed sequentially and one at a time.
    Emissions cannot be passed by an Observable concurrently or in parallel.
     */

    /*
    We can catch errors that occurs within our Observable.create() block and emit them through onError()
    In this way the error can be pushed up the chain and handled by the Observer
     */

    val sourceWithErrors: Observable<String> = Observable.create { emitter: ObservableEmitter<String> ->
        try {
            emitter.onNext("Alpha") //passing the string to the Observer
            emitter.onNext("Beta")
            emitter.onNext("Gamma")
            emitter.onNext("Delta")
            emitter.onNext("Epsilon")
            emitter.onComplete()
        } catch (error: Throwable) {
            emitter.onError(error)
        }
    }

    sourceWithErrors.subscribe({ println("RECEIVED: $it") }, Throwable::printStackTrace, { println("COMPLETED") })

    printSeparator()

    /*
    onNext, onComplete and onError do not necessarily push directly to the final Observer.
    They can also push to an operator serving as the next step in the chain.
    These operators (map, filter, ecc.) will yield a new Observable derived off the original Observable.
    The operators internally use Observer implementations to receive emissions.
     */

    val sourceForOperators: Observable<String> = Observable.create { emitter: ObservableEmitter<String> ->
        emitter.onNext("Alpha") //passing the string to the Observer
        emitter.onNext("Beta")
        emitter.onNext("Gamma")
        emitter.onNext("Delta")
        emitter.onNext("Epsilon")
        emitter.onComplete()
    }

    sourceForOperators.map(String::length)
        .subscribe {
            println("RECEIVED: $it")
        }

    printSeparator()

    /*
    We could have used the streamlined factory Observable.just instead of Observable.create
     */

    val sourceFromJust = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")

    sourceFromJust.map(String::length)
        .subscribe {
            println("RECEIVED: $it")
        }

    printSeparator()

    /*
    Emit the items from an Iterable type (such as List)
    It will call onNext() for each element and then call onComplete() after the iteration is complete
     */

    val list = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")

    val sourceFromIterator = Observable.fromIterable(list)

    sourceFromIterator.map(String::length)
        .filter { it >= 5 }
        .subscribe {
            println("RECEIVED: $it")
        }

    printSeparator()

    /*
    Each Observable returned by an operator is internally an Observer that receives, transforms and relays emissions to the next Observer downstream.
    When we talk about the Observer we are often talking about the final Observer at the end of the Observable chain that consumes the emissions.
    But each operator also implements Observer internally
     */

    val myFinalObserver = object : Observer<Int> {

        override fun onComplete() {
            println("Done!")
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
        }

        override fun onNext(t: Int) {
            println("RECEIVED: $t")
        }

        override fun onSubscribe(d: Disposable) {
            //ignore it for now
        }

    }

    source.map(String::length)
        .filter { it >= 5 }
        .subscribe(myFinalObserver)

    printSeparator()

    /*
    Instead of implementing an Observer, we can pass up to three lambda parameters separated by commas: the onNext lambda, the onError lambda, and the onComplete lambda.
    There are overloads that allow to omit onComplete and/or onError behaviours, and it will no longer perform any action for the omitted parameters
     */

    val onNext: Consumer<Int> = Consumer(::println)
    val onComplete = Action { println("Done!") }
    val onError: Consumer<Throwable> = Consumer(Throwable::printStackTrace)

    source.map(String::length)
        .filter { it >= 5 }
        .subscribe(onNext, onError, onComplete)

    printSeparator()

    /*
    Testing without handling the error
    RESULTS: in production the error must always be handled, otherwise the application would crash
     */

    val sourceWithError = Observable.create<String> { emitter ->
        try {
            emitter.onNext("Alpha") //passing the string to the Observer
            emitter.onNext("Beta")

            throw Exception("very unexpected error")

            emitter.onNext("Gamma")
            emitter.onNext("Delta")
            emitter.onNext("Epsilon")

        } catch (error: Throwable) {
            emitter.onError(error)
        } finally {
            emitter.onComplete()
        }
    }

    sourceWithError.map(String::length)
        .filter { it >= 5 }
        .subscribe(onNext, onError)

    /*
    Most of subscribe() overloads (generally those that accept the lambdas as parameters) return a Disposable.
    Disposables allow us to disconnect an Observable from an Observer so emissions are terminated early, which is critical for infinite or long-running Observables
     */

    printSeparator()

}

