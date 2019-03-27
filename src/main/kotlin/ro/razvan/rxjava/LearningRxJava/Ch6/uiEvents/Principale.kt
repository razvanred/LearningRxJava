package ro.razvan.rxjava.LearningRxJava.Ch6.uiEvents

import com.google.gson.Gson
import hu.akarnokd.rxjava2.swing.SwingObservable
import hu.akarnokd.rxjava2.swing.SwingSchedulers
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ro.razvan.rxjava.LearningRxJava.Ch6.uiEvents.model.Comment
import ro.razvan.rxjava.LearningRxJava.Ch6.uiEvents.model.User
import ro.razvan.rxjava.LearningRxJava.UsersApi
import ro.razvan.rxjava.LearningRxJava.writeOnFile
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*


object Test {
    @JvmStatic
    fun main(args: Array<String>) {

        val gson = Gson()

        Single.just(UsersApi.getUser())
            .subscribeOn(Schedulers.io())
            .doOnSuccess { println("Download completed on ${Thread.currentThread().name} ") }
            .observeOn(Schedulers.computation())
            .map { gson.fromJson(it, User::class.java) }
            .doOnSuccess { println("Data of the user ${it.profile.name} elaborated on ${Thread.currentThread().name}") }
            .map { it.comments.size }
            .doOnSuccess { println("The user has been posted $it comments, calculated on ${Thread.currentThread().name}") }
            .observeOn(Schedulers.io())
            .doOnSuccess { println("Writing the result on ${Thread.currentThread().name}") }
            .subscribe(Consumer { writeOnFile(it.toString(), "/users/razvan/Desktop/0.txt") })

        Thread.sleep(10_000)

    }
}

class Principale : JFrame() {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Principale()
        }

    }

    private val gson = Gson()
    private val refreshBtn: JButton
    private val defaultListModel: DefaultListModel<String>

    init {

        rootPane.layout = FlowLayout()

        refreshBtn = JButton("REFRESH")
        defaultListModel = DefaultListModel()
        val list = JList<String>(defaultListModel)

        rootPane.add(refreshBtn)
        rootPane.add(list)

        setRefreshListener()

        title = "Hello Swing Concurrency"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(300, 200)
        setLocationRelativeTo(null)

        show()

    }

    private fun setRefreshListener() {

        SwingObservable.actions(refreshBtn)
            .observeOn(Schedulers.io())
            .flatMapSingle {
                Observable.fromIterable(gson.fromJson(UsersApi.getUser(), User::class.java).comments)
                    .map(Comment::body)
                    .toList()
            }
            .observeOn(SwingSchedulers.edt())
            .subscribe(defaultListModel::addAll)

    }

}