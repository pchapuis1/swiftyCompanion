package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swiftycompanion.api.ApiClient
import com.example.swiftycompanion.api.OAuth2Manager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import java.io.IOException


class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.rootView).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }

        val editText = findViewById<EditText>(R.id.idLogin)
        val button = findViewById<Button>(R.id.idBtnSearch)
        button.setOnClickListener {
            val userId = editText.text.toString().trim()
            if (userId.isNotEmpty()) {
                button.isEnabled = false
                fetchUserData(userId)
            } else {
                Toast.makeText(this, "Please enter a login", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    private fun fetchUserData(userId: String) {
        OAuth2Manager.getAccessToken(this) { token, error ->
            runOnUiThread {
                if (token == null) {
                    if (error != null) {
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to retrieve access token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val button = findViewById<Button>(R.id.idBtnSearch)
                    button.isEnabled = true
                } else {
                    val apiService = ApiClient.create(token)
                    val call = apiService.getCursusUserById(userId)

                    call.enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            val button = findViewById<Button>(R.id.idBtnSearch)
                            button.isEnabled = true
                            if (response.isSuccessful) {
                                val user = response.body()
                                if (user != null) {
                                    val intent =
                                        Intent(this@MainActivity, UserDetailsActivity::class.java)
                                    val gson = Gson()
                                    val userJson = gson.toJson(user)
                                    intent.putExtra("user_json", userJson)
                                    startActivity(intent)
                                } else {
                                    Log.e("API_RESPONSE", "User is null")
                                    Toast.makeText(
                                        this@MainActivity,
                                        "User not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                when (response.code()) {
                                    401 -> Toast.makeText(
                                        this@MainActivity,
                                        "Unauthorized",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    404 -> Toast.makeText(
                                        this@MainActivity,
                                        "User not found",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    500 -> Toast.makeText(
                                        this@MainActivity,
                                        "Server error",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    else -> Toast.makeText(
                                        this@MainActivity,
                                        "Error: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                Log.e(
                                    "API_RESPONSE",
                                    "Error: ${response.code()} - ${response.message()}"
                                )
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            val button = findViewById<Button>(R.id.idBtnSearch)
                            button.isEnabled = true
                            if (t is IOException) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No internet connection",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Failed to fetch user",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Log.e("API_FAILURE", "Request failed", t)
                        }
                    })
                }
            }
        }
    }
}
