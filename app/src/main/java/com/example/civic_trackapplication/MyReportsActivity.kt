package com.example.civic_trackapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MyReportsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reports)

        val tv = findViewById<TextView>(R.id.tvReportStatus)
        tv.text = "You have submitted 3 issues. 1 Resolved, 2 Pending."
    }
}
