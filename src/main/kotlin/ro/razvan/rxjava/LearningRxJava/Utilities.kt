package ro.razvan.rxjava.LearningRxJava

fun printSeparator(length: Int = 10) {
    for (i in 0 until length)
        print("-")
    print("\n")
}

fun Thread.sleep(millis: Long) {
    java.lang.Thread.sleep(millis)
}