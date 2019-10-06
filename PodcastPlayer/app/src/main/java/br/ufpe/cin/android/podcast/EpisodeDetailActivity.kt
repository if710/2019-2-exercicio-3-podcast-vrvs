package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_episode_detail.*
import kotlinx.android.synthetic.main.itemlista.*
import kotlinx.android.synthetic.main.itemlista.item_title

class EpisodeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        item_title.text = intent.getStringExtra("title")
        item_description.text = intent.getStringExtra("description")
        item_link.text = intent.getStringExtra("link")

        item_description.movementMethod = ScrollingMovementMethod()
    }
}
