package com.droid.videoview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.droid.videoview.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: VideoAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchVideos()
    }

    private fun fetchVideos() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://fatema.takatakind.com/app_api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiInterface = retrofit.create(ApiInterface::class.java)
        val retrofitData = apiInterface.getAllVideos()

        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val list = responseBody?.msg ?: emptyList()

                    adapter = VideoAdapter(this@MainActivity, list, object : VideoAdapter.OnVideoPreparedListener {
                        override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                            exoPlayerItems.add(exoPlayerItem)
                        }
                    })

                    binding.vp.adapter = adapter

                    binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                            if (previousIndex != -1) {
                                val player = exoPlayerItems[previousIndex].exoPlayer
                                player.pause()
                                player.playWhenReady = false
                            }
                            val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                            if (newIndex != -1) {
                                val player = exoPlayerItems[newIndex].exoPlayer
                                player.playWhenReady = true
                                player.play()
                            }
                        }
                    })
                } else {
                    Log.d("MainActivity", "Response not successful: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Log.d("MainActivity", "onFailure: ${t.message}")
            }
        })
    }

    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.vp.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.vp.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }
}