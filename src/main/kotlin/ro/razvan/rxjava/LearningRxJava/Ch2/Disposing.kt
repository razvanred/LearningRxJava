package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.ResourceObserver
import java.util.concurrent.TimeUnit

/*
When you subscribe() to an Observable to receive emissions a stream is created to process these emissions through the Observable chain.
We want do dispose of these resources when we are done so that they can be garbage-collected.
The finite Observables that call onComplete() will typically dispose of themselves safely when they are done.
But if you are working with infinite or long-running Observables you likely will run into situations where you want to stop the emissions and dispose everything associated with that subscription.
Explicit disposal is necessary in order to prevent memory leaks.
 */

/*
The Disposable is a link between an Observable and an active Observer.
You can call its dispose() method to stop emissions and dispose of all resources used for that Observer.
It also has an isDisposed() method indicating whether it has been disposed of already..
 */

object ExplicitDispose {

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.interval(1, TimeUnit.SECONDS)
        val disposable =
            source.subscribe(::println) // the link between the Observable and the active Observer is returned by the method subscribe() that accepts up to three lambdas

        Thread.sleep(5000)

        disposable.dispose()
        println("Disposable disposed!")

        Thread.sleep(5000) // sleepThread 5 seconds to prove that there are no more emissions

    }

}

/*
The onSubscribe() method takes a Disposable as parameter.
It allows the Observer to have the ability to dispose of the subscription at any time.
You can implement your own Observer and use onNext(), onComplete() or onError() to have access to the Disposable.
These three events can call dispose() if, for whatever reason, the Observer does not want any emissions.
 */

object HandledDisposableInObserver {

    @JvmStatic
    fun main(args: Array<String>) {

        val observer = object : Observer<String> {

            lateinit var disposable: Disposable

            override fun onSubscribe(d: Disposable) {
                this.disposable = d
            }

            override fun onComplete() {
                //has access to disposable
            }

            override fun onError(e: Throwable) {
                //has access to disposable
            }

            override fun onNext(t: String) {
                //has access to disposable
                println(t)
                disposable.dispose()
            }

        }

        Observable.just("One", "Two", "Three")
            .subscribe(observer)

        /*
        OUTPUT: One
         */

        /*Each step in the Observable chain has access to the Disposable*/

        /*
        Passing an Observer to the subscribe() method will not return Disposable since it is assumed that the Observer will handle it.
         */

    }

}

/*
If you do not want to explicitly handle the Disposable and want RxJava to handle it for you (which is a good idea until you have the reason to take control), you can extend ResourceObserver as your Observer, which uses a default Disposable handling.
Pass this to subscribeWith() and you will get the default Disposable returned.
 */

object HandledDisposable {

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.interval(1, TimeUnit.SECONDS)

        val myObserver = object : ResourceObserver<Long>() {

            override fun onNext(t: Long) {
                println(t)
            }

            override fun onComplete() {
                println("Completed!")
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

        }

        val disposable = source.subscribeWith(myObserver)

        Thread.sleep(5000)

        disposable.dispose()
        println("Disposable disposed!")

        Thread.sleep(5000)

    }

}

/*
CompositeDisposable implements Disposable, but internally holds a collection of disposables, which you can add to and then dispose all at once.
Useful to manage and dispose of several subscriptions
 */

object CompositeDisposable {

    private val disposables = CompositeDisposable()

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.interval(1, TimeUnit.SECONDS)

        //different stream of data for each Observer, because Observable.interval is a cold Observable

        val disposable1 = source.subscribe { println("Observer 1: $it") }
        val disposable2 = source.subscribe { println("Observer 2: $it") }

        disposables.addAll(disposable1, disposable2)

        Thread.sleep(5000)

        disposables.dispose()
        println("Disposables disposed!")

        Thread.sleep(5000)

        /*
        OUTPUT:
        Observer 1: 0
        Observer 2: 0
        Observer 2: 1
        Observer 1: 1
        Observer 1: 2
        Observer 2: 2
        Observer 1: 3
        Observer 2: 3
        Observer 1: 4
        Observer 2: 4
        Disposables disposed!
         */

    }

}

object DisposalHandlingWhileCreatingObservable {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        it should be used Observable.range() here but this is just an example of the manual handling of the Disposal
         */

        val source = Observable.create<Int> { emitter ->
            try {
                for (i in 0 until 1000) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(i)
                    } else
                        return@create
                }
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }

        source.subscribe(::println, Throwable::printStackTrace) { println("Completed!") }

    }

}