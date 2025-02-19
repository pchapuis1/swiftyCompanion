package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class UserDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        val gson = Gson()
        val userJson = intent.getStringExtra("user_json")
        val user: User? = gson.fromJson(userJson, User::class.java)

        Log.d("User 2", "User: $user")

        if (user != null) {
            findViewById<TextView>(R.id.idLogin).text = user.login
            findViewById<TextView>(R.id.idFirstName).text = user.firstName
            findViewById<TextView>(R.id.idLastName).text = user.lastName
            findViewById<TextView>(R.id.idPhoneNumber).text = user.phone
            findViewById<TextView>(R.id.idEmail).text = user.email
            findViewById<TextView>(R.id.idWallet).text = "${user.wallet} ₳"
            findViewById<TextView>(R.id.idEvaluationPoint).text = user.correctionPoint.toString()


            val levelTextView = findViewById<TextView>(R.id.idLevelText)
            val levelBar = findViewById<ProgressBar>(R.id.idLevelBar)
            val gradeTextView = findViewById<TextView>(R.id.idGrade)

            val imgView = findViewById<ImageView>(R.id.imgLayout)
            Glide.with(this).load(user.image.versions.large).into(imgView)

            // Set spinner with cursus names
            val spinner = findViewById<Spinner>(R.id.idCursus)
            val cursusList = user.cursusUsers
            val cursusNames = cursusList.map { it.cursus.name }

            val adapter = object : ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                cursusNames
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val text = view.findViewById<TextView>(android.R.id.text1)
                    text.setTextColor(Color.WHITE)
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val text = view.findViewById<TextView>(android.R.id.text1)
                    return view
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Set highest level cursus as default
            val highestLevelCursus = cursusList.maxByOrNull { it.level }
            highestLevelCursus?.let { selectedCursus ->
                val initialIndex = cursusList.indexOf(selectedCursus)
                spinner.setSelection(initialIndex)

                updateCursusInfo(selectedCursus, levelTextView, levelBar, gradeTextView)
                fillSkillTable(selectedCursus)
                fillProjectTable(user, selectedCursus.cursus.id)
            }

            // event listener on Spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedCursus = cursusList[position]
                    updateCursusInfo(selectedCursus, levelTextView, levelBar, gradeTextView)
                    fillSkillTable(selectedCursus)
                    fillProjectTable(user, selectedCursus.cursus.id)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

//      Back Button
        val backButton = findViewById<ImageView>(R.id.idBackBtn)
        val backText = findViewById<TextView>(R.id.idBackText)
        val goBackListener = View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        backButton.setOnClickListener(goBackListener)
        backText.setOnClickListener(goBackListener)
    }

    private fun updateCursusInfo(cursus: CursusUser, levelTextView: TextView, levelBar: ProgressBar, gradeTextView: TextView) {
        gradeTextView.text = cursus.grade ?: "N/A"
        val levelText = "Level ${cursus.level.toInt()} - ${(cursus.level % 1 * 100).toInt()}%"
        levelTextView.text = levelText
        levelBar.progress = (cursus.level % 1 * 100).toInt()
    }

    private fun calculateMonthsAgo(dateString: String?): Long {
        return if (dateString != null) {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val projectDate = LocalDateTime.parse(dateString, formatter).toLocalDate()
            val currentDate = LocalDate.now()
            ChronoUnit.MONTHS.between(projectDate, currentDate)
        } else {
            0
        }
    }

    private fun fillProjectTable(user: User, cursusId: Int) {
        val tableLayout: TableLayout = findViewById(R.id.projectTable)
        tableLayout.removeAllViews()

        if (user != null) {
            user.projectsUsers.forEach { project ->
                if (project.status != "finished" || project.cursus_ids[0] != cursusId)
                    return@forEach

                val linearLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, 14, 0, 14)
                }

                val projectNameTextView = TextView(this).apply {
                    text = project.project.name
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(Color.parseColor("#00B3B4"))

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                }

                val monthsAgoTextView = TextView(this).apply {
                    val monthsAgo = calculateMonthsAgo(project.marked_at)
                    text = "$monthsAgo months ago"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTextColor(Color.parseColor("#000000"))

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                    )
                    setPadding(20, 0, 0,0)
                }

                val markTextView = TextView(this).apply {
                    text = project.final_mark.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTypeface(null, Typeface.BOLD)
                    if (project.final_mark != null && project.final_mark >= 70)
                        setTextColor(Color.parseColor("#5CB85C"))
                    else
                        setTextColor(Color.parseColor("#FF0000"))

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                }

                linearLayout.addView(projectNameTextView)
                linearLayout.addView(monthsAgoTextView)
                linearLayout.addView(markTextView)

                tableLayout.addView(linearLayout)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillSkillTable(cursus: CursusUser) {
        val tableLayout: TableLayout = findViewById(R.id.skillTable)
        tableLayout.removeAllViews()

        cursus.skills.forEach { skill ->
            val linearLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 14, 0, 14)
            }

            val skillNameTextView = TextView(this).apply {
                text = skill.name
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.parseColor("#00B3B4"))

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }

            val progressContainer = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    400, 80
                )
            }

            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                max = 20 * 100
                progress = (skill.level * 100).toInt()
                progressDrawable = ContextCompat.getDrawable(context, R.drawable.level_bar_style)
            }

            val progressTextView = TextView(this).apply {
                text = "Level ${"%.2f".format(skill.level)} - ${(skill.level / 20 * 100).toInt()}%"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }

            progressContainer.addView(progressBar)
            progressContainer.addView(progressTextView)

            linearLayout.addView(skillNameTextView)
            linearLayout.addView(progressContainer)
            tableLayout.addView(linearLayout)
        }
    }
}