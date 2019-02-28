package ro.razvan.rxjava.LearningRxJava.Ch3

import com.google.common.collect.ImmutableList
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import ro.razvan.rxjava.LearningRxJava.printSeparator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/*
Collection operators will accumulate all emissions into a collection such as a list or map and then emit that entire collection as a Single emission.
These operators are another from of reducing operators since they consolidate emissions into a single one.
 */

object ToList {

    /*
    The operator toList() will take the emissions from the Observable<T>, it will collect them and then emit the entire collection wrapped into a Single.
    By default, this operator will emit Single<List<T>> (it will use a standard ArrayList implementation).
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("Roma", "Milano", "Napoli", "Torino")
            .map(String::length)
            .toList(4) //it can be specified the capacityHint parameter in order to optimize the initialization of the ArrayList (if the number is wrong it will handle the situation itself)
            .subscribe(Consumer(::println))

        printSeparator()

        /*
        If you want to specify a different list implementation besides ArrayList you can provide a Callable lambda as an argument to construct one
         */
        Observable.just(1, 2, 3)
            .toList {
                return@toList CopyOnWriteArrayList<Int>()
            }.subscribe(Consumer(::println))

    }

}

object ToSortedList {

    data class Person(val name: String, val age: Int) : Comparable<Person> {

        override fun compareTo(other: Person): Int {
            return name.compareTo(other.name)
        }

    }

    object PersonAgeComparator : Comparator<Person> {

        override fun compare(o1: Person?, o2: Person?): Int {

            if (o1 == null && o2 == null)
                return 0

            if (o1 == null)
                return 1

            if (o2 == null)
                return -1

            return o1.age.compareTo(o2.age)
        }

    }

    /*
    The operator toSortedList() is a flavour of the operator toList().
    It will collect the emissions into a list that sorts the items based on their Comparable implementation
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just(
            Person("Razvan", 7),
            Person("Anna", 5)
        )
            .toSortedList(PersonAgeComparator, 2)
            .subscribe(Consumer(::println))

    }

}

object ToMap {

    /*
    For a given Observable<T> the toMap() operator will collect emissions into Map<K, T> where K is the key type derived off a lambda Function<T,K> argument producing the key for each emission.
    If we want to yield a different value other than the emission to associate with the key, we can provide a second lambda argument that maps each emission to a different value.
    By default, toMap() will use HashMap. We can also provide a third lambda argument that provides a different lambda implementation.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        val source = Observable.just("Hello", "Sunshine", "How", "Are", "You")

        source
            .toMap({
                it[0]
            }, String::length) { ConcurrentHashMap<Char, Int>() }
            .subscribe(Consumer(::println))

        /*
        OUTPUT: {A=3, S=8, H=3, Y=3}
         */

        printSeparator()

        /*
        NOTE: there were two words with the same key, but it has been kept the length of the last one.
        To keep the length of both of them using the same key consider using toMultimap(), it will create an array for each key.
         */

        source
            .toMultimap(
                { it[0] }, String::length
            )
            .subscribe(Consumer(::println))


    }

}

object Collect {

    /*
    The collect() operator is useful to collect emissions into any arbitrary type that RxJava does not provide out of the box.
     */

    @JvmStatic
    fun main(args: Array<String>) {

        Observable.just("Alpha", "Beta", "Gamma")
            .collect({ HashSet<String>() }) { acc, act -> acc.add(act) }
            .subscribe(Consumer(::println))

        printSeparator()

        /*
        Use collect() instead of reduce when you are putting emissions into a mutable object you need a new mutable object seed each time.
         */

        /*
        Using the ImmutableList of the Google Guava library
         */

        Observable.just("Alpha", "Beta", "Gamma")
            .collect({ ImmutableList.Builder<String>() }) { acc, act -> acc.add(act) }
            .map(ImmutableList.Builder<String>::build)
            .subscribe(Consumer(::println))

    }

}