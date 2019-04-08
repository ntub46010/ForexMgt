package com.vincent.forexmgt

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vincent.forexmgt.service.BookService
import com.vincent.forexmgt.service.EntryService

class ForExMgtApp : Application() {

    override fun onCreate() {
        super.onCreate()

        bindService(Intent(this, BookService::class.java), bookServiceConn, Context.BIND_AUTO_CREATE)
        bindService(Intent(this, EntryService::class.java), entryServiceConn, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        unbindService(bookServiceConn)
        unbindService(entryServiceConn)
    }

    companion object {
        var bookService: BookService? = null
        var entryService: EntryService? = null
        var currentLoginUser: FirebaseUser? = null
    }

    private val bookServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bookService = (service as BookService.CollectionBinder).getService()
            bookService?.setLoginUser(currentLoginUser)
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    private val entryServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            entryService = (service as EntryService.CollectionBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }
}