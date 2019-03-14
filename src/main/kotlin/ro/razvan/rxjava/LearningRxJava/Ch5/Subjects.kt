package ro.razvan.rxjava.LearningRxJava.Ch5

import io.reactivex.Observable
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.UnicastSubject
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.TimeUnit

/*
Subjects are the mutable variables of reactive programming.
Just like mutable variables are necessary at times even though you should strive for immutability, Subjects are sometimes a necessary tool to reconcile imperative paradigms with reactive ones.
 */

object PublishSubjects {

    /*
    Subject is an abstract type that implements both Observable and Observer.
    This means you can call manually onNext(), onComplete() and onError() on a Subject, and it will, in turn, pass those events downstream toward its Observers.

    PublishSubject, like all Subjects, hotly broadcasts to its downstream Observers.
    PublishSubject, comparing to other Subject implementations, is the vanilla type, if you will.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        test1()

        printSeparator()

        test2()

        /*
        IMPORTANT NOTE: the first Observable to call onComplete() on Subject is going to cease other Observables from pushing their emissions, and downstream cancellation requests are ignored.
        This means that you will most likely use Subjects for infinite, event-driven Observables (like user action-driven events, the management of an UI).
         */

        printSeparator()

        test3()

    }

    private fun test3() {

        /*
        All the emissions will be missed by the Observer in this case, because the Subject is hot
         */

        val source = io.reactivex.subjects.PublishSubject.create<String>()

        with(source) {

            onNext("One")
            onNext("TWo")
            onNext("Three")

            onComplete()

        }

        source.map(String::length)
            .subscribe(::println, Throwable::printStackTrace) { println("done.") }

    }

    private fun test2() {

        /*
        You will use Subjects to eagerly subscribe to an unknown number of multiple source Observables and consolidate their emissions as a single Observable.
        Since Subjects are an Observer, you can pass them to a subscribe() method easily.

        This can be very helpful in modularized code bases where decoupling between Observables and Observer takes place and executing Observable.merge() is not that easy.
         */

        val subject = io.reactivex.subjects.PublishSubject.create<String>()

        val source1 = Observable.interval(300, TimeUnit.MILLISECONDS).map { "${(it + 1) * 300} milliseconds" }
        val source2 = Observable.interval(1, TimeUnit.SECONDS).map { "${it + 1} seconds" }

        val disposable = subject.subscribe(::println)

        source1.subscribe(subject) // they will return Unit, because the subject is an implementation of the Observer interface
        source2.subscribe(subject) // the management of the disposable is delegated to the Observer implementation

        Thread.sleep(3000)

        disposable.dispose()

    }

    private fun test1() {

        val subject = io.reactivex.subjects.PublishSubject.create<String>()

        subject.map(String::length)
            .subscribe(::println, Throwable::printStackTrace) { println("done.") }

        with(subject) {

            onNext("One")
            onNext("Two")
            onNext("Three")

            onComplete()

        }

    }

}

object SerializingSubjects {

    /*
    A critical gotcha to note with Subjects is that the onSubscribe(), onNext(), onError() and onComplete() calls are not threadsafe
    Emissions could start to overlap and break the Observable contract, which demands that emissions happen sequentially.
    The toSerialized() method yields a safety-serialized Subject implementation
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val subject = io.reactivex.subjects.PublishSubject.create<String>().toSerialized()

        subject.subscribe(::println)

        with(subject) {

            onNext("One")
            onNext("Two")
            onNext("Three")

            onComplete()

        }

    }

}

object BehaviorSubjects {

    /*
    BehaviorSubject behaves almost the same way as PublishSubject, but it will replay the last emitted item to each new Observer downstream.
    It is like putting replay(1).autoConnect() after a PublishSubject, but it consolidates these operations into a single optimized Subject implementation that subscribes eagerly to the source.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val subject = BehaviorSubject.create<String>()

        subject.subscribe({ println("Observer 1: $it") }, Throwable::printStackTrace) { println("Observer 1: done") }

        with(subject) {

            onNext("One")
            onNext("Two")
            onNext("Three")

        }

        subject.subscribe({ println("Observer 2: $it") }, Throwable::printStackTrace) { println("Observer 2: done") }

        subject.onComplete()

        /*
        OUTPUT:
        Observer 1: One
        Observer 1: Two
        Observer 1: Three
        Observer 2: Three
        Observer 1: done
        Observer 2: done
         */

    }

}

object ReplaySubjects {

    /*
    ReplaySubject is similar to PublishSubject followed by a cache() operator.
    It immediately captures emissions regardless of the presence of downstream Observers and optimizes the caching to occur inside the Subject itself.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val subject = ReplaySubject.create<String>()

        subject.subscribe { println("Observer 1: $it") }

        with(subject) {

            onNext("Alpha")
            onNext("Beta")
            onNext("Gamma")

            onComplete()

        }

        subject.subscribe { println("Observer 2: $it") }

    }

}

object AsyncSubjects {

    /*
    AsyncSubject has a highly tailored, finite-specific behavior: it will only push the last value it receives, followed by an onComplete() event.
    This is a Subject you do not want to use with infinite sources since it only emits when onComplete() is called.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val subject = AsyncSubject.create<String>()

        subject.subscribe({ println("Observer 1: $it") }, Throwable::printStackTrace) { println("Observer 1 done!") }

        with(subject) {

            onNext("Alpha")
            onNext("Beta")
            onNext("Gamma")

            onComplete()

        }

        subject.subscribe({ println("Observer 2: $it") }, Throwable::printStackTrace) { println("Observer 2 done!") }

    }

}

object UnicastSubjects {

    /*
    A UnicastSubject, like all Subjects, will be used to observe and subscribe to the sources.
    But it will buffer all the emissions it receives until an Observer subscribes to it, and then it will release all these emissions to the Observer and clear its cache.
    This subject works only with one exception, otherwise it will throw an exception.
    If you want the cached emissions to be emitted on another Observer use ReplaySubject.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        test1()

        printSeparator()

        test2()

    }

    private fun test1() {

        val subject = UnicastSubject.create<String>()

        Observable.interval(300, TimeUnit.MILLISECONDS)
            .map { (it + 1) * 300 }
            .map { "$it milliseconds" }
            .subscribe(subject)

        Thread.sleep(2_000)

        val disposable = subject.subscribe { println("Observer 1: $it") }

        Thread.sleep(2_000)

        disposable.dispose()

    }

    private fun test2() {

        /*
        Using a UnicastSubject to take the cached emissions on an Observer, and then allow another Observer to connect and receive the live emissions (without receiving the cached emissions)
         */

        val subject = UnicastSubject.create<String>()

        Observable.interval(300, TimeUnit.MILLISECONDS)
            .map { (it + 1) * 300 }
            .map { "$it milliseconds" }
            .subscribe(subject)

        Thread.sleep(2000)

        val multicast = subject.publish().autoConnect()

        val disposable0 = multicast.subscribe { println("Observer 1: $it") }

        Thread.sleep(2000)

        val disposable1 = multicast.subscribe { println("Observer 2: $it") }

        Thread.sleep(2000)

        disposable0.dispose()
        disposable1.dispose()

        /*
        OUTPUT:
        Observer 1: 300 milliseconds
        Observer 1: 600 milliseconds
        Observer 1: 900 milliseconds
        Observer 1: 1200 milliseconds
        Observer 1: 1500 milliseconds
        Observer 1: 1800 milliseconds
        Observer 1: 2100 milliseconds
        Observer 1: 2400 milliseconds
        Observer 1: 2700 milliseconds
        Observer 1: 3000 milliseconds
        Observer 1: 3300 milliseconds
        Observer 1: 3600 milliseconds
        Observer 1: 3900 milliseconds
        Observer 1: 4200 milliseconds
        Observer 2: 4200 milliseconds
        Observer 1: 4500 milliseconds
        Observer 2: 4500 milliseconds
        Observer 1: 4800 milliseconds
        Observer 2: 4800 milliseconds
        Observer 1: 5100 milliseconds
        Observer 2: 5100 milliseconds
        Observer 1: 5400 milliseconds
        Observer 2: 5400 milliseconds
        Observer 1: 5700 milliseconds
        Observer 2: 5700 milliseconds
        Observer 1: 6000 milliseconds
        Observer 2: 6000 milliseconds
         */

    }

}