package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("DownloadService") {

    public override fun onHandleIntent(i: Intent?) {
        try {
            //Serviço para fazer o Download
            val root = getExternalFilesDir(DIRECTORY_DOWNLOADS)
            root?.mkdirs()
            val output = File(root, i!!.data!!.lastPathSegment!!)
            if (output.exists()) {
                output.delete()
            }
            val url = URL(i.data!!.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                var size = c.contentLength
                while (len >= 0) {
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)
                }
                out.flush()
            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            //Salvando o diretorio no DB
            val db = ItemFeedDB.getDatabase(applicationContext)
            db.itemFeedDao().addPath(i.getStringExtra("title"), output.path)

            val intent = Intent(DOWNLOAD_COMPLETE)
            intent.putExtra("title", i.getStringExtra("title"))

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        } catch (e2: IOException) {
            Log.e(javaClass.getName(), "Exception durante download", e2)
        }

    }

    companion object {
        val DOWNLOAD_COMPLETE = "br.ufpe.cin.android.podcast.DOWNLOAD_COMPLETE"
    }
}