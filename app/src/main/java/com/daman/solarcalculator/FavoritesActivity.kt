package com.daman.solarcalculator

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daman.solarcalculator.data.AppDatabase
import com.daman.solarcalculator.data.DbWorkerThread
import com.daman.solarcalculator.data.entities.UserLocations
import kotlinx.android.synthetic.main.activity_favorites.*
import android.app.Activity
import android.view.MenuItem
import com.daman.solarcalculator.util.toast


class FavoritesActivity : AppCompatActivity(), FavoriteAdapter.FavoriteListener {
    companion object {
        const val USER_LOCATION_CLICKED = "USER_LOCATION"
    }

    private val mDb: AppDatabase by lazy { AppDatabase.getInstance(this) }
    private val mDbWorkerThread: DbWorkerThread by lazy { DbWorkerThread("dbWorkerThread") }
    private val mUiHandler = Handler()
    private val list = ArrayList<UserLocations>()
    private val adapter = FavoriteAdapter(this@FavoritesActivity, list, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        title = "Favorites"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDbWorkerThread.start()

        favoriteRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        favoriteRecyclerView.adapter = adapter

        getLocationsFromDb()
    }

    private fun getLocationsFromDb() {
        val task = Runnable {
            val dbList = mDb.userLocationsDao().getAll()
            mUiHandler.post {
                if (dbList != null && dbList.isNotEmpty()) {
                    list.clear()
                    list.addAll(dbList)
                    adapter.notifyDataSetChanged()
                } else {
                    "No pinned locations found!".toast(this@FavoritesActivity)
                }
            }
        }
        mDbWorkerThread.postTask(task)
    }

    override fun onItemClicked(userLocations: UserLocations) {
        val returnIntent = Intent()
        returnIntent.putExtra(USER_LOCATION_CLICKED, userLocations)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
