package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync

class ItemFeedAdapter (private val itemFeeds: List<ItemFeed>, private val main: MainActivity) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {

    var musicPlayerService: MusicPlayerWithBindingService? = null

    override fun getItemCount(): Int = itemFeeds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(main.applicationContext).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = itemFeeds[position]
        holder.title.text = p.title
        holder.date.text = p.pubDate
        val episode = itemFeeds[position]
        doAsync {
            val db = ItemFeedDB.getDatabase(main.applicationContext)
            val item = db.itemFeedDao().search(p.title)
            holder.play.isEnabled = (item.path != "")
            holder.download.isEnabled = (item.path == "")
        }
        holder.itemView.setOnClickListener{
            val intent = Intent(
                main.applicationContext,
                EpisodeDetailActivity::class.java
            )
            intent.putExtra("title", episode.title)
            intent.putExtra("description", episode.description)
            intent.putExtra("link", episode.link)
            main.startActivity(intent)
        }

        //para fazer o download
        holder.download.setOnClickListener{
            Toast.makeText(main.applicationContext, "Downloading ${episode.title}..", Toast.LENGTH_SHORT).show()
            val downloadService = Intent(main.applicationContext, DownloadService::class.java).apply {
                putExtra("title", p.title)
            }
            downloadService.data = Uri.parse(episode.link)
            main.startService(downloadService)
        }

        holder.play.setOnClickListener{
            main.musicPlayerService!!.playMusic(p.path, holder.play, p.title, holder)
        }

        val filter = IntentFilter(DownloadService.DOWNLOAD_COMPLETE)
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getStringExtra("title") == p.title) {
                    holder.play.isEnabled = true
                    holder.download.isEnabled = false
                    doAsync {
                        val db = ItemFeedDB.getDatabase(main.applicationContext)
                        val item = db.itemFeedDao().search(p.title)
                        p.path = item.path
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(main).registerReceiver(broadcastReceiver, filter)
    }

    class ViewHolder (item : View) : RecyclerView.ViewHolder(item) {
        val title : TextView = item.findViewById(R.id.item_title)
        val date : TextView = item.findViewById(R.id.item_date)
        val download : Button = item.findViewById(R.id.item_action)
        val play : ImageButton = item.findViewById(R.id.imageButton2)
    }
}