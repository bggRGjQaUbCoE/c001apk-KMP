package logic.model

data class FeedContentResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val messageStatus: String?,
    val data: HomeFeedResponse.Data?,
)

