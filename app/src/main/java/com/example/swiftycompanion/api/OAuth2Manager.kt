package com.example.swiftycompanion.api

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Properties

fun saveAccessToken(context: Context, token: String) {
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("access_token", token)
    editor.apply()
}

fun getAccessToken(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("access_token", null)
}

object SecretsManager {
    private var properties: Properties? = null

    fun loadSecrets(context: Context) {
        if (properties == null) {
            properties = Properties()
            try {
                val inputStream = context.assets.open("secrets.properties")
                properties?.load(inputStream)
            } catch (e: Exception) {
                throw RuntimeException("Error loading secrets.properties", e)
            }
        }
    }

    fun get(key: String): String {
        return properties?.getProperty(key) ?: throw RuntimeException("Missing key: $key in secrets.properties")
    }
}

object OAuth2Manager {
    private lateinit var UID: String
    private lateinit var SECRET: String
    private const val TOKEN_URL = "https://api.intra.42.fr/oauth/token"

    fun init(context: Context) {
        SecretsManager.loadSecrets(context)
        UID = SecretsManager.get("UID")
        SECRET = SecretsManager.get("SECRET")
    }

    fun getAccessToken(context: Context, callback: (String?, String?) -> Unit) {
        if (!this::UID.isInitialized || !this::SECRET.isInitialized) {
            init(context)
        }

        val storedToken = getAccessToken(context)
        if (storedToken != null) {
            callback(storedToken, null)
            return
        }
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", UID)
            .add("client_secret", SECRET)
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OAuth2Manager", "Network error", e)
                callback(null, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    when (response.code) {
                        401 -> {
                            Log.e("OAuth2Manager", "Unauthorized access: 401")
                            callback(null, "Unauthorized access: Check your client ID and secret.")
                        }
                        else -> {
                            Log.e("OAuth2Manager", "HTTP error: ${response.code} - ${response.message}")
                            callback(null, "HTTP error: ${response.code} - ${response.message}")
                        }
                    }
                    return
                }
                val responseBody = response.body?.string()
                try {
                    val json = JSONObject(responseBody ?: "")
                    val accessToken = json.optString("access_token")
                    saveAccessToken(context, accessToken)
                    callback(accessToken, null)
                } catch (e: JSONException) {
                    Log.e("OAuth2Manager", "JSON parsing error", e)
                    callback(null, "JSON parsing error: ${e.message}")                }
            }
        })
    }
}
