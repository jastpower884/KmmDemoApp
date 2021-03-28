package com.jastzeonic.kmmdemoapp.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jastzeonic.kmmdemoapp.shared.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var launchesRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var errorMessage: TextView

    private val sdk = SpaceXSDK(DatabaseDriverFactory(this))

    private val launchesRvAdapter = LaunchesRvAdapter(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "SpaceX Launches"
        setContentView(R.layout.activity_main)

        launchesRecyclerView = findViewById(R.id.launchesListRv)
        swipeRefreshLayout = findViewById(R.id.swipeContainer)
        errorMessage = findViewById(R.id.errorMessage)
        launchesRecyclerView.adapter = launchesRvAdapter

        displayLaunches()
    }

    private fun displayLaunches() {
        lifecycleScope.launch(Dispatchers.Main) {
            swipeRefreshLayout.isRefreshing = true
            runCatching {
                sdk.getLaunches(false)
            }.onSuccess {
                launchesRvAdapter.launches = it
                launchesRvAdapter.notifyDataSetChanged()
            }.onFailure {
                errorMessage.text = "$it"
            }
            swipeRefreshLayout.isRefreshing = false
        }

    }
}

class LaunchesRvAdapter(var launches: List<RocketLaunch>) :
    RecyclerView.Adapter<LaunchesRvAdapter.LaunchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaunchViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_launch, parent, false)
            .run(::LaunchViewHolder)
    }

    override fun getItemCount(): Int = launches.count()

    override fun onBindViewHolder(holder: LaunchViewHolder, position: Int) {
        holder.bindData(launches[position])
    }

    inner class LaunchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val missionNameTextView = itemView.findViewById<TextView>(R.id.missionName)
        private val launchYearTextView = itemView.findViewById<TextView>(R.id.launchYear)
        private val launchSuccessTextView = itemView.findViewById<TextView>(R.id.launchSuccess)
        private val missionDetailsTextView = itemView.findViewById<TextView>(R.id.details)

        fun bindData(launch: RocketLaunch) {
            val ctx = itemView.context
            missionNameTextView.text =
                ctx.getString(R.string.mission_name_field, launch.missionName)
            launchYearTextView.text =
                ctx.getString(R.string.launch_year_field, launch.launchYear.toString())
            missionDetailsTextView.text =
                ctx.getString(R.string.details_field, launch.details ?: "")
            val launchSuccess = launch.launchSuccess
            if (launchSuccess != null) {
                if (launchSuccess) {
                    launchSuccessTextView.text = ctx.getString(R.string.successful)
                    launchSuccessTextView.setTextColor(
                        (ContextCompat.getColor(
                            itemView.context,
                            R.color.colorSuccessful
                        ))
                    )
                } else {
                    launchSuccessTextView.text = ctx.getString(R.string.unsuccessful)
                    launchSuccessTextView.setTextColor(
                        (ContextCompat.getColor(
                            itemView.context,
                            R.color.colorUnsuccessful
                        ))
                    )
                }
            } else {
                launchSuccessTextView.text = ctx.getString(R.string.no_data)
                launchSuccessTextView.setTextColor(
                    (ContextCompat.getColor(
                        itemView.context,
                        R.color.colorNoData
                    ))
                )
            }
        }
    }
}