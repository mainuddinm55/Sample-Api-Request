package info.learncoding.jazzcashaskquestion

import android.content.ContentResolver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            val multipartBody = prepareQuestionBody(
                prepareFilePart(arrayOf()),
                Question(
                    "Hi, I am feeling un-well since friday, now what can i do?",
                    "2019519",
                    "text"
                )
            )
            val progressRequestBody = ProgressRequestBody(multipartBody) {

            }

            GlobalScope.launch(Dispatchers.IO) {
                val response = try {
                    buildClient().askQuestion(getHeaders(), progressRequestBody)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (response != null) {
                    Log.d("MainActivity", "Question post successful")
                }
                Log.d("MainActivity", "onCreate:ask question-> $response")

            }


        }
    }


    private fun prepareFilePart(files: Array<String>): MultipartBody.Builder {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        files.forEachIndexed { index, path ->
            val file = File(path)
            builder.addFormDataPart(
                "images[$index]", file.name, RequestBody.create(
                    MediaType.get(getMimeType(Uri.fromFile(file)) ?: "image/*"),
                    file
                )
            )
        }
        return builder
    }

    private fun prepareQuestionBody(
        builder: MultipartBody.Builder,
        question: Question?,
    ): MultipartBody {
        builder.addFormDataPart("question[user_id]", question?.user_id ?: "")
        builder.addFormDataPart("question[body]", question?.body ?: "")
        builder.addFormDataPart("question[type]", question?.type ?: "text")
        return builder.build()
    }

    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr: ContentResolver = applicationContext.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase(Locale.getDefault())
            )
        }
    }

    private fun buildClient(): Api {
        val loggerInterceptor = HttpLoggingInterceptor()
        loggerInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(loggerInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://sandbox.maya-apa.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(Api::class.java)
    }

    private fun getHeaders(): HashMap<String, String> {
        val header = hashMapOf<String, String>()
        header["app-key"] = "698abd7505b333f1fc5c2224bb42fc979ee7a0c"
        header["access-token"] =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjVjNTE0YmVjNjZiNjdmYjUwOGIwODQ4Mjk0ZTk3MTNkZDU3YWY1ZGE0ZWY3MTM3YjZjMWE1ZGJkY2VkOWE2ZDU3MzU0ZjQyNWU1MDEzMzIzIn0.eyJhdWQiOiI3NCIsImp0aSI6IjVjNTE0YmVjNjZiNjdmYjUwOGIwODQ4Mjk0ZTk3MTNkZDU3YWY1ZGE0ZWY3MTM3YjZjMWE1ZGJkY2VkOWE2ZDU3MzU0ZjQyNWU1MDEzMzIzIiwiaWF0IjoxNjIzNjE4Mjg0LCJuYmYiOjE2MjM2MTgyODQsImV4cCI6MTY1NTE1NDI4NCwic3ViIjoiMjAxOTUxOSIsInNjb3BlcyI6W119.hK88lU8YY3q0E8LLsvsqkPWK897G1EkOt7HoCQwpGE4VyNrKu7OqV_hoi4Tz-0s7Mns_fMNjtmx8iywePCCWuYQz8YbbFhBloj7zs_yiQaJWJEIP6PjyoVIJHS5XT0f-sPAUjx2dyGtdkSHAwifVZNTAkYGlSeA0sTpIAPCntUZZ0me8ZKWJn3ukGrRFi10BTXDG4MHiQ2pNiIKXVoBXx0ov7Kc3GdfVcUbMIsc-TNsqQJOMP6wuF8p3gh8qlldgCqXFEuGiWNCD1x3k73jAIzfOi2qDUjPqjmEaKWVaIiF2casBPoBQ-Fqxh-Gf63FjCzg8yYNNq-Yyk2M50CfTB_shVPmmb4VdNF_YLScu-eVqoXsbWEx20MtRiC7ZVc_629-_CkHua5oc2ckjBPpt8De0CSTUDio-AHf4GbFBK3HOmCuxgdcPhAUSPyh4l4x6TRwcOCBjX29kzuWCNvx_a9wbHp334jD5OlU6gwV-Xem5SKHjQ94qtErZgAXvwg9RcmpD9M9ZKUwmgLFdY7YnW_tm3igTPznEWu-woQbYLlJSj5wVCQUGs0ijZRFRYfzFy6MrwjOvP43wXuHZfFObdurrKiUfV-7Otxi2npEB3T1XVxqNpkbx4wVMJcawobcBS9axzOnbfTqlbHNSW0YWHWGnqqxBnObazMmGAkHAhDY"
        return header
    }
}
