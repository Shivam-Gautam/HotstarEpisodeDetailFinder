# Hotstar Episode Finder

## Overview

Hotstar Episode Finder is a simple Android application that allows users to fetch and display metadata for a specific TV show episode from Hotstar. It demonstrates how to make API calls to a web service, handle JSON responses, and display the information in a user-friendly way. The primary use case is to quickly look up details like episode title, description, duration, and season/episode number given an episode's Content ID and the series name.

## Features

*   Fetch episode metadata using a Content ID and Series Name.
*   Displays:
    *   Series Name and Episode Title
    *   Season and Episode Number (if available in API response)
    *   Episode Duration (if available)
    *   Episode Description
    *   Episode Content ID
*   User interface with input fields and a dedicated display area for results.
*   Uses default values for quick testing of a known episode ("House - Pilot").

## Core Logic & Implementation Details

### 1. API Interaction

*   **Networking Library:** The app uses **Retrofit** as a type-safe HTTP client to define and make API calls to the Hotstar service. **OkHttp** is used as the underlying HTTP client for Retrofit, allowing for advanced configuration.
*   **API Endpoint:** The base URL for the API is `https://www.hotstar.com/`.
*   **Headers:** Crucially, the Hotstar API requires specific HTTP headers for authentication and proper functioning. These headers (e.g., `User-Agent`, `x-hs-usertoken`, `X-HS-Client`, etc.) are hardcoded in `MainActivity.kt` and added to every outgoing request using an OkHttp `Interceptor`.
    *   **Note:** These headers, especially the `x-hs-usertoken`, are likely to be temporary or tied to a specific user session. They might expire or change, which could cause the API calls to fail (e.g., with a 401 Unauthorized error).
*   **Request:** The app makes a GET request to an endpoint structured similar to `/o/v2/multi/content/common/bundle-playback` (the exact path is defined in `HotstarApiService.kt`), passing the episode `contentId` and `searchQuery` (series name) as query parameters.

### 2. Data Parsing

*   **JSON Handling:** The API response is in JSON format. **GsonConverterFactory** (used with Retrofit) automatically parses the JSON response into Kotlin data classes defined in `HotstarResponse.kt`.
*   **Data Classes (`HotstarResponse.kt`):** These classes model the expected JSON structure, allowing easy access to nested data like episode title, description, tags, etc.

### 3. Information Extraction & Display

*   **`MainActivity.kt`:** This is the main activity and contains the core logic:
    *   **UI Initialization:** Sets up `EditText` fields for input, `TextViews` for output, and a `Button` to trigger the fetch operation.
    *   **Fetching Data:** When the "Fetch" button is clicked:
        1.  It retrieves the `contentId` and `seriesName` from the input fields.
        2.  Calls the `getHotstarContent` method from `HotstarApiService`.
        3.  Handles the API response asynchronously using Retrofit's `enqueue` method.
    *   **Processing Response:**
        *   If the response is successful (HTTP 200-299) and the data is parsable:
            *   It navigates through the nested data objects (from `HotstarResponse.kt`) to find the relevant playable item.
            *   Extracts `title`, `description`, `contentId`.
            *   Attempts to find specific `tags` that represent the **duration** (e.g., "42m") and **season/episode number** (e.g., "S1 E1") using regular expressions.
        *   The extracted information is then displayed in `seriesInfoTextView` and `metadataTextView`.
    *   **Error Handling:** Basic error handling is in place to show messages for API errors (non-successful HTTP codes) or network failures.
    *   **Logging:** `HttpLoggingInterceptor` (OkHttp) is used to log network request and response information to Logcat (currently set to `BASIC` level for less verbose output), which is helpful for debugging.

### 4. User Interface (`activity_main.xml`)

*   A `ConstraintLayout` is used to arrange UI elements.
*   Includes:
    *   A title `TextView` for the app.
    *   Labeled `EditText` fields for "Episode Content ID" and "Series Name".
    *   A "Fetch" `Button`.
    *   `TextViews` (`seriesInfoTextView`, `metadataTextView`) to display the fetched information in a structured way.

## How to Use

1.  **Input Episode Content ID:** Enter the specific ID for the Hotstar episode you want to look up (e.g., `1971036132` for "House - Pilot").
2.  **Input Series Name:** Enter the name of the TV series. This is used as the `search_query` in the API call to provide context (e.g., `house`).
3.  **Tap Fetch:** Click the "Fetch" button.
4.  **View Results:** The application will display:
    *   The series name and the fetched episode's title.
    *   Details such as Season/Episode, Duration, Description, and the episode's Content ID.

## Key Files

*   `app/src/main/java/com/project/videometafinder/MainActivity.kt`: Contains the main application logic, UI interactions, and API call orchestration.
*   `app/src/main/java/com/project/videometafinder/HotstarApiService.kt`: Defines the Retrofit service interface for the Hotstar API.
*   `app/src/main/java/com/project/videometafinder/HotstarResponse.kt`: Contains the Kotlin data classes that model the JSON response from the API.
*   `app/src/main/res/layout/activity_main.xml`: Defines the user interface layout.
*   `app/src/main/res/values/strings.xml`: Contains string resources, including the app name.
*   `app/build.gradle`: Includes dependencies for Retrofit, OkHttp, and Gson.

## Setup and Build

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Let Gradle sync the dependencies.
4.  Build and run the application on an Android device or emulator.

## Known Limitations & Considerations

*   **API Stability & Headers:** The Hotstar API endpoint and required headers are not officially documented for public use and may change without notice. The hardcoded headers (especially the user token) are prone to expiry, which will cause API calls to fail. This app is primarily for demonstration and learning purposes.
*   **Error Handling:** Error handling is basic. A production app would require more robust error management and user feedback.
*   **Data Availability:** The presence and format of specific details like duration or season/episode tags can vary in the API response.

---

*This README was generated with assistance from an AI tool.*