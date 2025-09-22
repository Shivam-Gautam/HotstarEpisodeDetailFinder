package com.project.videometafinder

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private lateinit var hotstarUrlEditText: EditText
    private lateinit var fetchButton: Button
    private lateinit var seriesInfoTextView: TextView
    private lateinit var metadataTextView: TextView

    private lateinit var hotstarApiService: HotstarApiService

    // Default URL for quick testing
    private val DEFAULT_HOTSTAR_URL = "https://www.hotstar.com/in/shows/house/1971003083/occams-razor/1971036174/watch"

    // IMPORTANT: Replace with your actual token. Leaving it as a placeholder will likely cause 401 Unauthorized errors.
    private val HS_USER_TOKEN = "YOUR_HS_USER_TOKEN_PLACEHOLDER" 
    private val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
    private val HS_DEVICE_ID = "5e744-70dc3e-634592-6ddc89"
    private val HS_CLIENT = "platform:web;app_version:25.09.11.1;browser:Chrome;schema_version:0.0.1556;os:Mac OS;os_version:10.15.7;browser_version:140;network_data:4g"
    private val HS_PLATFORM = "web"
    private val HS_APP = "250911001"
    private val COUNTRY_CODE = "in"
    private val ACCEPT_LANGUAGE = "eng"
    private val REQUEST_ID = "7dd6d-3d71ba-8fb3ef-48ee24"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        hotstarUrlEditText = findViewById(R.id.hotstarUrlEditText)
        fetchButton = findViewById(R.id.fetchButton)
        seriesInfoTextView = findViewById(R.id.seriesInfoTextView)
        metadataTextView = findViewById(R.id.metadataTextView)

        hotstarUrlEditText.setText(DEFAULT_HOTSTAR_URL)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val headerInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
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
            
            // Only add the token if it's not the placeholder
            if (HS_USER_TOKEN != "YOUR_HS_USER_TOKEN_PLACEHOLDER") {
                requestBuilder.header("x-hs-usertoken", HS_USER_TOKEN)
            } else {
                Log.w("MainActivity", "HS_USER_TOKEN is a placeholder. API calls may fail or be rate-limited.")
            }
            
            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.hotstar.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        hotstarApiService = retrofit.create(HotstarApiService::class.java)

        fetchButton.setOnClickListener {
            val url = hotstarUrlEditText.text.toString().trim()
            if (url.isNotBlank() && Patterns.WEB_URL.matcher(url).matches()) {
                parseUrlAndFetchContent(url)
            } else {
                clearEpisodeDetails()
                seriesInfoTextView.text = "Invalid URL"
                metadataTextView.text = "Please enter a valid Hotstar episode URL."
                Toast.makeText(this, "Invalid URL format", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun parseUrlAndFetchContent(url: String) {
        val pattern = Pattern.compile("/shows/([^/]+)/[^/]+/([^/]+)/([^/]+)/watch")
        val matcher: Matcher = pattern.matcher(url)

        if (matcher.find()) {
            val seriesSlug = matcher.group(1)
            val episodeId = matcher.group(3)
            
            if (seriesSlug != null && episodeId != null) {
                Log.d("MainActivity", "Parsed Series Slug: $seriesSlug, Episode ID: $episodeId")
                fetchHotstarContent(episodeId, seriesSlug) 
            } else {
                showUrlParseError()
            }
        } else {
            showUrlParseError()
        }
    }

    private fun showUrlParseError() {
        clearEpisodeDetails()
        seriesInfoTextView.text = "URL Parse Error"
        metadataTextView.text = "Could not extract series and episode ID from the URL."
        Toast.makeText(this, "Could not parse URL", Toast.LENGTH_SHORT).show()
    }

    private fun fetchHotstarContent(requestedContentId: String, seriesNameQuery: String) {
        seriesInfoTextView.text = "Fetching: '$seriesNameQuery' (ID: $requestedContentId)..."
        metadataTextView.text = "Loading..."

        hotstarApiService.getHotstarContent(contentId = requestedContentId, searchQuery = seriesNameQuery)
            .enqueue(object : Callback<HotstarResponse> {
                override fun onResponse(call: Call<HotstarResponse>, response: Response<HotstarResponse>) {
                    if (response.isSuccessful) {
                        val hotstarData = response.body()
                        val allItems = hotstarData?.success?.space?.widgetWrappers
                            ?.firstOrNull()?.widget?.data?.trayItems?.data?.items

                        val playableItem = allItems?.find { 
                            it.playableContent?.data?.contentId == requestedContentId 
                        }?.playableContent?.data

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

                            seriesInfoTextView.text = "Series: $seriesNameQuery - $episodeTitle"
                            
                            metadataTextView.text = ""
                            appendDetail(metadataTextView, "Season/Episode", seasonEpisodeTag)
                            appendDetail(metadataTextView, "Duration", durationTag)
                            appendDetail(metadataTextView, "Description", description, singleLine = false)
                            appendDetail(metadataTextView, "Content ID", currentEpisodeId)

                        } else {
                            clearEpisodeDetails()
                            seriesInfoTextView.text = "Episode Not Found"
                            metadataTextView.text = "Could not find details for Episode ID: $requestedContentId in series '$seriesNameQuery'."
                            Log.w("MainActivity", "Episode with ID '$requestedContentId' not found. Response: ${response.body().toString().take(500)}")
                        }
                    } else {
                        clearEpisodeDetails()
                        seriesInfoTextView.text = "API Error: ${response.code()}"
                        metadataTextView.text = "API Error: ${response.message()} (Code: ${response.code()})\nCheck Logcat for details."
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

    private fun appendDetail(textView: TextView, label: String, value: String, singleLine: Boolean = true) {
        val currentText = textView.text.toString()
        if (currentText.isNotEmpty()) {
            textView.append("\n")
            if (!singleLine) textView.append("\n") 
        }
        textView.append("$label: $value")
    }

    private fun clearEpisodeDetails() {
        seriesInfoTextView.text = ""
        metadataTextView.text = "Details will appear here..."
    }
}