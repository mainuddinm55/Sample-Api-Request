package info.learncoding.jazzcashaskquestion

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST


interface Api {
    @POST("api/v1/jazzcash/store/question")
    suspend fun askQuestion(
        @HeaderMap headers: HashMap<String, String>,
        @Body requestBody: RequestBody
    ): AskQuestionResponse
}