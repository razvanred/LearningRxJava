package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable
import io.reactivex.ObservableEmitter

fun main() {

    val source: Observable<String> = Observable.create { emitter: ObservableEmitter<String> ->
        emitter.onNext("Alpha")
        emitter.onNext("Beta")
        emitter.onNext("Gamma")
        emitter.onNext("Delta")
        emitter.onNext("Epsilon")
        emitter.onComplete()
    }

    source.subscribe { println("RECEIVED: $it") }

}
