package ro.razvan.rxjava.LearningRxJava.Ch2

import com.github.davidmoten.rx.jdbc.ConnectionProviderFromUrl
import com.github.davidmoten.rx.jdbc.Database
import io.reactivex.Observable
import ro.razvan.rxjava.LearningRxJava.printSeparator

object ColdObservables {

    /*
    Cold Observables are much like a music CD that can be replayed to each listener, so each person can hear all the tracks at any time
     */

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        Two Observers subscribed to one Observable
         */
        val source = Observable.just("One", "Two", "Three")

        source.subscribe { println("Observable 1 receives: $it") }
        source.subscribe { println("Observable 2 receives: $it") }

        /*
        RESULT:
        Observable 1 receives: One
        Observable 1 receives: Two
        Observable 1 receives: Three
        Observable 2 receives: One
        Observable 2 receives: Two
        Observable 2 receives: Three
         */

        /*
        Cold Observables replay the emissions to each observer, ensuring that all Observers get all the data.
        Most data-driven Observables are cold, and this includes the Observable.just() and Observable.fromIterator() factories.

        Observable sources that emit finite datasets are usually cold
         */

    }

    object SQLiteTest {

        @JvmStatic
        fun main(args: Array<String>) {

            val connection = ConnectionProviderFromUrl("jdbc:sqlite:/Users/razvan/test.db").get()
            val db = Database.from(connection)

            val customerNames = db.select("SELECT NAME FROM Customers")
                .getAs(String::class.java)

            customerNames.subscribe { println("First subscription: $it") }
            printSeparator()
            customerNames.subscribe { println("Second subscription: $it") }

            /*
            RxJava-JDBC will run the query each time for each Observer.
            This means that if the data changes in between two subscriptions, the second Observer get different emissions than the first one.
            The Observable is still cold since it is replaying the query event if the resulting data changes.
             */

        }

    }

}