package ro.razvan.rxjava.LearningRxJava.Ch6.uiEvents.model

data class User(
    val comments: List<Comment>,
    val posts: List<Post>,
    val profile: Profile
)