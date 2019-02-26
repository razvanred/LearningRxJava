package ro.razvan.rxjava.LearningRxJava.Ch2

import io.reactivex.Observable
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.VBox
import javafx.stage.Stage


object HotObservables {

    /*
    A Hot Observable is like a radio station, it broadcasts the same emissions to all Observers at the same time.
    If an Observer subscribes to an Hot Observable, receives some emissions, and then another Observer comes in afterwards, that second Observer will have missed those emissions.
    Hot Observables often represent events rather than finite datasets.
     */

    class JavaFxApp : Application() {

        companion object {
            @JvmStatic
            fun main(args: Array<String>) {
                launch(JavaFxApp::class.java)
            }

            /*
            This method is included in the RxJavaFX library
            ObservableValue has nothing to do with io.reactivex.Observable
             */
            private fun <T> valuesOf(fxObservable: ObservableValue<T>): Observable<T> {

                return Observable.create { emitter ->

                    //emit initial state
                    emitter.onNext(fxObservable.value)

                    //emit value changes uses a listener
                    val listener = ChangeListener<T> { _, _, newValue ->
                        emitter.onNext(newValue)
                    }

                    fxObservable.addListener(listener)

                }

            }
        }

        override fun start(primaryStage: Stage?) {

            val toggleButton = ToggleButton("TOGGLE ME")
            val label = Label()

            val selectedStates: Observable<Boolean> = valuesOf(toggleButton.selectedProperty())
            selectedStates.map { if (it) "DOWN" else "UP" }
                .subscribe(label::setText)

            val vBox = VBox(toggleButton, label)

            primaryStage?.scene = Scene(vBox)
            primaryStage?.show()

        }

    }

}