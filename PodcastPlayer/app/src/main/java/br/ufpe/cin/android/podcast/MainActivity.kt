package br.ufpe.cin.android.podcast

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {

    internal var musicPlayerService: MusicPlayerWithBindingService? = null
    internal var isBound = false
    private lateinit var itemFeedAdapter: ItemFeedAdapter

    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            musicPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as MusicPlayerWithBindingService.MusicBinder
            musicPlayerService = binder.service
            isBound = true
            itemFeedAdapter.musicPlayerService = musicPlayerService
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val musicServiceIntent = Intent(this, MusicPlayerWithBindingService::class.java)
        startService(musicServiceIntent)

        setContentView(R.layout.activity_main)

        itemFeedAdapter = ItemFeedAdapter(listOf(), this)

        recycler_view.adapter = itemFeedAdapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        var main : MainActivity = this

        var itemFeedList : List<ItemFeed>
        val db = ItemFeedDB.getDatabase(applicationContext)

        doAsync {
            try {
                val xml = URL("https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml").readText()
                itemFeedList = Parser.parse(xml, main)

                itemFeedList.forEach {
                    db.itemFeedDao().insert(it)
                }
            } catch (e: Exception) {
                itemFeedList = db.itemFeedDao().findAll()
            }


            uiThread {
                recycler_view.adapter = ItemFeedAdapter(itemFeedList, main)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(applicationContext, MusicPlayerWithBindingService::class.java)
            isBound = bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        unbindService(sConn)
        super.onStop()
    }
}
