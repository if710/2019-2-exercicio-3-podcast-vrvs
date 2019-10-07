package br.ufpe.cin.android.podcast

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ItemFeedAdapter (private val itemFeeds: List<ItemFeed>, private val main: MainActivity) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {

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
        holder.download.setOnClickListener{
            Toast.makeText(main.applicationContext, "Downloading ${episode.title}..", Toast.LENGTH_SHORT).show()
            val downloadService = Intent(main.applicationContext, DownloadService::class.java)
            downloadService.data = Uri.parse(episode.link)
            main.startService(downloadService)
        }
    }

    class ViewHolder (item : View) : RecyclerView.ViewHolder(item), View.OnClickListener {
        val title : TextView = item.findViewById(R.id.item_title)
        val date : TextView = item.findViewById(R.id.item_date)
        val download : Button = item.findViewById(R.id.item_action)

        init {
            download.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Toast.makeText(v.context, "Downloading ${title.text}..", Toast.LENGTH_SHORT).show()
        }
    }
}