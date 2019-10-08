package br.ufpe.cin.android.podcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.ImageButton
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.FileInputStream

class MusicPlayerWithBindingService : Service() {
    private var mPlayer: MediaPlayer? = null
    private var title : String? = null
    private var podcastPath :String? = null
    private var button : ImageButton? = null
    private var holder : ItemFeedAdapter.ViewHolder? = null
    private val mBinder = MusicBinder()

    override fun onCreate() {
        super.onCreate()
        // configurar media player
        mPlayer = MediaPlayer()

        //fica em loop
        mPlayer?.isLooping = true

        //excluindo os arquivos após ouvir e setando os botões para funcionar ou não
        mPlayer?.setOnCompletionListener {
            doAsync {

                val db = ItemFeedDB.getDatabase(applicationContext)
                db.itemFeedDao().addPath(title!!, "")
                db.itemFeedDao().addDuration(title!!, 0)

                holder!!.play.setImageResource(R.drawable.ic_play_icon)
                holder!!.play.isEnabled = false
                holder!!.download.isEnabled = true

                val podcastFile = File(podcastPath!!)
                if (podcastFile.exists()) {
                    podcastFile.delete()
                }
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerReceiver(playReceiver, IntentFilter(PLAY_ACTION))
        registerReceiver(pauseReceiver, IntentFilter(PAUSE_ACTION))
        return Service.START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }

    fun setNotification(t: String, action: String) {
        val imageAction = if(action == PAUSE_ACTION) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val actionName = if(action == PLAY_ACTION) {
            "Play"
        } else {
            "Pause"
        }

        val actionIntent = Intent(action)
        actionIntent.putExtra("title", t)
        val actionPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, actionIntent, 0)

        val notificationIntent = Intent(applicationContext, MusicPlayerWithBindingService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        createChannel()

        val notification = NotificationCompat.Builder(
            applicationContext,"1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(NotificationCompat.Action(imageAction, actionName, actionPendingIntent))
            .setOngoing(true)
            .setContentTitle("Você está escutando")
            .setContentText(t)
            .setContentIntent(pendingIntent).build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        startForeground(NOTIFICATION_ID, notification)
    }

    fun playMusic(podcastPath: String, imageButton: ImageButton, title: String, holder: ItemFeedAdapter.ViewHolder) {
        this.title = title
        this.button = imageButton
        this.podcastPath = podcastPath
        this.holder = holder
        doAsync {
            if (!mPlayer!!.isPlaying) {
                val fis = FileInputStream(podcastPath)
                mPlayer?.reset()
                mPlayer?.setDataSource(fis.fd)
                mPlayer?.prepare()
                val db = ItemFeedDB.getDatabase(applicationContext)
                val item = db.itemFeedDao().search(title)
                mPlayer?.seekTo(item.duration)
                fis.close()
                mPlayer?.start()
                imageButton.setImageResource(R.drawable.ic_pause_icon)
                setNotification(title, PAUSE_ACTION)
            } else {
                mPlayer?.pause()
                val db = ItemFeedDB.getDatabase(applicationContext)
                db.itemFeedDao().addDuration(title, mPlayer?.currentPosition!!)
                imageButton.setImageResource(R.drawable.ic_play_icon)
                setNotification(title, PLAY_ACTION)
            }
        }
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicPlayerWithBindingService
            get() = this@MusicPlayerWithBindingService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel("1", "Canal de Notificacoes", NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.description = "Descricao"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playMusic(podcastPath!!, button!!, title!!, holder!!)
        }
    }

    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playMusic(podcastPath!!, button!!, title!!, holder!!)
        }
    }

    companion object {
        private val NOTIFICATION_ID = 2
        const val PLAY_ACTION = "br.ufpe.cin.android.podcast.PLAY_ACTION"
        const val PAUSE_ACTION = "br.ufpe.cin.android.podcast.PAUSE_ACTION"
    }

}