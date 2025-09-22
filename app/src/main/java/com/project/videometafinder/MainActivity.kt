package com.project.videometafinder

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var contentIdEditText: EditText
    private lateinit var searchQueryEditText: EditText
    private lateinit var fetchButton: Button
    private lateinit var seriesInfoTextView: TextView
    private lateinit var metadataTextView: TextView

    private lateinit var hotstarApiService: HotstarApiService

    // Default values for quick testing
    private val DEFAULT_CONTENT_ID = "1971036132" // Episode ID for "House - Pilot"
    private val DEFAULT_SEARCH_QUERY = "house"     // Series Name for context

    // --- API Headers - These are crucial for the Hotstar API to work ---
    private val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
    private val HS_USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6IiIsImF1ZCI6InVtX2FjY2VzcyIsImV4cCI6MTc1ODYwODQzNCwiaWF0IjoxNzU4NTIyMDM0LCJpc3MiOiJUUyIsImp0aSI6IjI4OGM4YWVlYjRhMTQ5YjU4YTk4MGIyNjc4MTdmZmZlIiwic3ViIjoie1wiaElkXCI6XCJiZTA0NzI3NTg3ZmI0NzNmYmI3NDEzODRhZDRhOWIzZlwiLFwicElkXCI6XCIwNTAyZGZhMGQ1NmE0ZmEwOGExZDA5MTVjOWE1YTlhN1wiLFwiZHdIaWRcIjpcIjU5ODEzZjQzMjFlM2I3ZGJkNGNkZmFhZmM5ODZmN2RiM2FlMWFlNmVlZDQ5OWJiZTkzODU0MDQzNjNmZTk5ZGZcIixcImR3UGlkXCI6XCJjZmE0NGVmMmI4MDhhZTMyN2Y5NTRjNjNmNjQ5MDk0ZTA1YTY2MjIyMzc3ZmQxN2MwOGFkODczMjM1ODFlM2UyXCIsXCJvbGRIaWRcIjpcImJlMDQ3Mjc1ODdmYjQ3M2ZiYjc0MTM4NGFkNGE5YjNmXCIsXCJvbGRQaWRcIjpcIjA1MDJkZmEwZDU2YTRmYTA4YTFkMDkxNWM5YTVhOWE3XCIsXCJpc1BpaVVzZXJNaWdyYXRlZFwiOmZhbHNlLFwibmFtZVwiOlwiU2hpdmFtXCIsXCJwaG9uZVwiOlwiODg2Njc3NDQxOVwiLFwiaXBcIjpcIjI0MDE6NDkwMDoxY2I4OjYxOmJkOWE6MjdiMjo1ZjlhOjllZDlcIixcImNvdW50cnlDb2RlXCI6XCJpblwiLFwiY3VzdG9tZXJUeXBlXCI6XCJudVwiLFwidHlwZVwiOlwicGhvbmVcIixcImlzRW1haWxWZXJpZmllZFwiOmZhbHNlLFwiaXNQaG9uZVZlcmlmaWVkXCI6dHJ1ZSxcImRldmljZUlkXCI6XCI1ZTc0NC03MGRjM2UtNjM0NTkyLTZkZGM4OVwiLFwicHJvZmlsZVwiOlwiQURVTFRcIixcInZlcnNpb25cIjpcInYyXCIsXCJzdWJzY3JpcHRpb25zXCI6e1wiaW5cIjp7XCJIb3RzdGFyU3VwZXJcIjp7XCJzdGF0dXNcIjpcIlNcIixcImV4cGlyeVwiOlwiMjAyNS0xMC0xOVQxNzoxNjoyOS4wMDBaXCIsXCJzaG93QWRzXCI6XCIxXCIsXCJjbnRcIjpcIjFcIn19fSxcImVudFwiOlwiQ3UwQkNnVUtBd29CQUJMakFSSUhZVzVrY205cFpCSURhVzl6RWdOM1pXSVNDV0Z1WkhKdmFXUjBkaElHWm1seVpYUjJFZ2RoY0hCc1pYUjJFZ1J0ZDJWaUVnZDBhWHBsYm5SMkVnVjNaV0p2Y3hJR2FtbHZjM1JpRWdSeWIydDFFZ2RxYVc4dGJIbG1FZ3BqYUhKdmJXVmpZWE4wRWdSMGRtOXpFZ1J3WTNSMkVnTnFhVzhTQm10bGNHeGxjaElFZUdKdmVCSUxjR3hoZVhOMFlYUnBiMjRTREdwcGIzQm9iMjVsYkdsMFpSSU5abVZoZEhWeVpXMXZZbWxzWlJvQ2MyUWFBbWhrR2dObWFHUWlBM05rY2lvR2MzUmxjbVZ2S2doa2IyeGllVFV1TVNvS1pHOXNZbmxCZEcxdmMxZ0JDcjBCQ2dVS0F3b0JCUkt6QVJJSFlXNWtjbTlwWkJJRGFXOXpFZ04zWldJU0NXRnVaSEp2YVdSMGRoSUdabWx5WlhSMkVnZGhjSEJzWlhSMkVnUnRkMlZpRWdkMGFYcGxiblIyRWdWM1pXSnZjeElHYW1sdmMzUmlFZ1J5YjJ0MUVnZHFhVzh0YkhsbUVncGphSEp2YldWallYTjBFZ1IwZG05ekVnUndZM1IyRWdOcWFXOFNCbXRsY0d4bGNob0NjMlFhQW1oa0dnTm1hR1FpQTNOa2Npb0djM1JsY21WdktnaGtiMnhpZVRVdU1Tb0taRzlzWW5sQmRHMXZjMWdCQ2cwU0N3Z0NPQUpBQVZDNENGZ0JDaUlLR2dvSUlnWm1hWEpsZEhZS0RoSUZOVFU0TXpZU0JUWTBNRFE1RWdRNFpGZ0JFbmdJQVJESSsrUHJuek1hUlFvYVNHOTBjM1JoY2xOMWNHVnlMa2xPTGpOTmIyNTBhQzR5T1RrU0RFaHZkSE4wWVhKVGRYQmxjaG9FVTJWc1ppREk4N1Q0NURJb3lQdmo2NTh6TUFZNEEwRFNIeWdCTUFFNklBb2NTRzkwYzNSaGNsQnlaVzFwZFcwdVNVNHVNMDF2Ym5Sb0xqUTVPUkFCU0FFPVwiLFwiaXNzdWVkQXRcIjoxNzU4NTIyMDM0MDQ2LFwibWF0dXJpdHlMZXZlbFwiOlwiQVwiLFwiZHBpZFwiOlwiMDUwMmRmYTBkNTZhNGZhMDhhMWQwOTE1YzlhNWE5YTdcIixcInN0XCI6MSxcImRhdGFcIjpcIkNnUUlBQklBQ2dRSUFDb0FDZ1FJQURvQUNoSUlBQ0lPZ0FFV2lBRUJrQUhBNnVlZXdEQUtMZ2dBUWlvS0tFSmtaakJoWkRaa01UbGpPVEEwTkRVNVltSmlNemt3WlRRNFpqRmpPREV6TWpKb1puTk9Sbk1LQkFnQU1nQT1cIn0iLCJ2ZXJzaW9uIjoiMV8wIn0.kZRRdLZe7gYwvQ_PzVepUIvHhQRxDkkpSjAG0DlhyjE"
    private val HS_DEVICE_ID = "5e744-70dc3e-634592-6ddc89"
    private val HS_CLIENT = "platform:web;app_version:25.09.11.1;browser:Chrome;schema_version:0.0.1556;os:Mac OS;os_version:10.15.7;browser_version:140;network_data:4g"
    private val HS_PLATFORM = "web"
    private val HS_APP = "250911001"
    private val COUNTRY_CODE = "in"
    private val ACCEPT_LANGUAGE = "eng"
    private val REQUEST_ID = "7dd6d-3d71ba-8fb3ef-48ee24"
    // --- End of API Headers ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        contentIdEditText = findViewById(R.id.contentIdEditText)
        searchQueryEditText = findViewById(R.id.searchQueryEditText)
        fetchButton = findViewById(R.id.fetchButton)
        seriesInfoTextView = findViewById(R.id.seriesInfoTextView)
        metadataTextView = findViewById(R.id.metadataTextView)

        // Set default input values
        contentIdEditText.setText(DEFAULT_CONTENT_ID)
        searchQueryEditText.setText(DEFAULT_SEARCH_QUERY)

        // Setup OkHttpClient with interceptors
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // Reduced verbosity for cleaner logs
        }

        val headerInterceptor = Interceptor { chain ->
            val requestWithHeaders = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("x-hs-usertoken", HS_USER_TOKEN)
                .header("x-hs-device-id", HS_DEVICE_ID)
                .header("X-HS-Client", HS_CLIENT)
                .header("X-HS-Platform", HS_PLATFORM)
                .header("x-hs-app", HS_APP)
                .header("X-Country-Code", COUNTRY_CODE)
                .header("X-HS-Accept-language", ACCEPT_LANGUAGE)
                .header("X-Request-Id", REQUEST_ID)
                .header("x-hs-request-id", REQUEST_ID)
                .header("sec-ch-ua-platform", "\"macOS\"")
                .header("sec-ch-ua", "\"Chromium\";v=\"140\", \"Not=A?Brand\";v=\"24\", \"Google Chrome\";v=\"140\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("Accept", "application/json, text/plain, */*")
                .header("x-hs-is-retry", "false")
                .header("x-hs-retry-count", "0")
                .build()
            chain.proceed(requestWithHeaders)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .build()

        // Setup Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.hotstar.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        hotstarApiService = retrofit.create(HotstarApiService::class.java)

        // Setup Fetch button click listener
        fetchButton.setOnClickListener {
            val contentId = contentIdEditText.text.toString().trim()
            val seriesName = searchQueryEditText.text.toString().trim()

            if (contentId.isNotBlank() && seriesName.isNotBlank()) {
                fetchHotstarContent(contentId, seriesName)
            } else {
                clearEpisodeDetails()
                seriesInfoTextView.text = "Input Incomplete"
                metadataTextView.text = "Please provide both Episode Content ID and Series Name."
            }
        }

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Fetches content from Hotstar API and updates the UI.
     */
    private fun fetchHotstarContent(contentId: String, seriesName: String) {
        seriesInfoTextView.text = "Fetching: '$seriesName' (ID: $contentId)..."
        metadataTextView.text = "Loading..."

        hotstarApiService.getHotstarContent(contentId = contentId, searchQuery = seriesName)
            .enqueue(object : Callback<HotstarResponse> {
                override fun onResponse(call: Call<HotstarResponse>, response: Response<HotstarResponse>) {
                    if (response.isSuccessful) {
                        val hotstarData = response.body()
                        val playableItem = hotstarData?.success?.space?.widgetWrappers
                            ?.firstOrNull()?.widget?.data?.trayItems?.data?.items
                            ?.firstOrNull()?.playableContent?.data

                        if (playableItem != null) {
                            val episodeTitle = playableItem.title ?: "N/A"
                            val description = playableItem.description ?: "N/A"
                            val currentEpisodeId = playableItem.contentId ?: "N/A"
                            
                            val durationTag = playableItem.tags?.firstOrNull {
                                it.value?.let { v -> v.contains(Regex("\\d")) && (v.endsWith("m") || v.endsWith("s")) } == true
                            }?.value ?: "N/A"

                            val seasonEpisodeTag = playableItem.tags?.firstOrNull {
                                it.value?.matches(Regex("S\\d+\\s*E\\d+", RegexOption.IGNORE_CASE)) == true
                            }?.value ?: "N/A"

                            seriesInfoTextView.text = "Series: $seriesName - $episodeTitle"
                            
                            metadataTextView.text = ""
                            appendDetail(metadataTextView, "Season/Episode", seasonEpisodeTag)
                            appendDetail(metadataTextView, "Duration", durationTag)
                            appendDetail(metadataTextView, "Description", description, singleLine = false)
                            appendDetail(metadataTextView, "Content ID", currentEpisodeId)

                        } else {
                            clearEpisodeDetails()
                            seriesInfoTextView.text = "Not Found"
                            metadataTextView.text = "No playable episode found for ID: $contentId in series '$seriesName'."
                            Log.w("MainActivity", "No playable item. Response body: ${response.body().toString()}")
                        }
                    } else {
                        clearEpisodeDetails()
                        seriesInfoTextView.text = "API Error"
                        metadataTextView.text = "API Error: ${response.code()} ${response.message()}"
                        Log.e("MainActivity", "API Error ${response.code()}: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<HotstarResponse>, t: Throwable) {
                    clearEpisodeDetails()
                    seriesInfoTextView.text = "Network Error"
                    metadataTextView.text = "Network request failed: ${t.message}"
                    Log.e("MainActivity", "Network Failure", t)
                }
            })
    }

    /**
     * Appends a labeled detail to a TextView, handling newlines.
     */
    private fun appendDetail(textView: TextView, label: String, value: String, singleLine: Boolean = true) {
        val currentText = textView.text.toString()
        if (currentText.isNotEmpty()) {
            textView.append("\n")
            if (!singleLine) textView.append("\n") // Extra newline for multi-line values like description
        }
        textView.append("$label: $value")
    }

    /**
     * Clears the details displayed in the UI.
     */
    private fun clearEpisodeDetails() {
        seriesInfoTextView.text = ""
        metadataTextView.text = "Details will appear here..."
    }
}