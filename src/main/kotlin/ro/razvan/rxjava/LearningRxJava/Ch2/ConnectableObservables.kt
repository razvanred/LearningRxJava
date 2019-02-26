package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable

object ConnectableObservables {

    /*
    ConnectableObservable takes any Observable, even if is cold, and make it hot so that emissions are played to all Observers at once.
    This conversion is made with the publish() method, and it will yield a ConnectableObservable.
    By subscribing it will not start the emissions yet.
    The connect() method allows to start firing the emissions. This allows us to set up all our Observers beforehand.
     */
    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.just("One", "Two", "Three").publish()

        source.subscribe { println("First Observer: $it") }
        source.map(String::length).subscribe { println("Second Observer: $it") }

        source.connect()

        /*
        RESULT:
        First Observer: One
        Second Observer: 3
        First Observer: Two
        Second Observer: 3
        First Observer: Three
        Second Observer: 5
         */

        /*
        Each emission goes to each Observer simultaneously (multicasting).
         */

    }

}