package com.betterfilter.linktester

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.httpGet
import okhttp3.OkHttpClient
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.customView
import android.R.string
import okhttp3.RequestBody
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.text.InputType
import okhttp3.Request
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val links = mutableListOf<Link>()
    lateinit var recyclerView: RecyclerView

    val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        links.addAll(getStoredLinks().map { Link(it, null) })

        recyclerView = find(R.id.recyclerView)
        recyclerView.adapter = LinkRecyclerViewAdapter(links) {
            deleteButtonClicked(it)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    fun getStoredLinks(): Set<String> {
        return defaultSharedPreferences.getStringSet("stored-links", mutableSetOf<String>()) ?: mutableSetOf<String>()
    }

    fun updateStoredLinks(newLinks: Collection<String>) {
/*        val oldLinks = getStoredLinks()
        val allLinks = HashSet<String>()
        allLinks.addAll(oldLinks)
        allLinks.addAll(newLinks)*/
        val allLinks = HashSet<String>()
        allLinks.addAll(links.map { it.link })
        with(defaultSharedPreferences.edit()) {
            remove("stored-links")
            apply()
            putStringSet("stored-links", allLinks)
            apply()
        }
    }

    fun updateStoredLinks() {
        updateStoredLinks(links.map { it.link })
    }

    fun deleteButtonClicked(link: Link) {
        links.remove(link)
        recyclerView.adapter?.notifyDataSetChanged()
        updateStoredLinks()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.addLink) {
            // add link
            alert {
                lateinit var editText: EditText
                customView {
                    editText = editText {
                        inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        maxLines = 1
                    }
                }
                yesButton {
                    links.add(Link(editText.text.toString(), null))
                    recyclerView.adapter = LinkRecyclerViewAdapter(links) {
                        deleteButtonClicked(it)
                    }

                    updateStoredLinks()
                }
                noButton {

                }
            }.show()
        } else if (id == R.id.startAll) {
            //start tests
            links.forEachIndexed { index, link ->
                link.isLoading = true
                recyclerView.adapter?.notifyItemChanged(index)

                doAsync {
                    try {
                        val request = Request.Builder()
                            .url(link.link)
                            .build()
                        client.newCall(request).execute()
                            .use { response ->
                                link.time =
                                    (response.receivedResponseAtMillis - response.sentRequestAtMillis).toString()
                                uiThread {
                                    link.isLoading = false
                                    recyclerView.adapter?.notifyItemChanged(index)
                                }
                            }
                    } catch (e: Exception) {
                        uiThread {
                            link.time = "Error"
                            link.isLoading = false
                            recyclerView.adapter?.notifyItemChanged(index)
                        }
                    }
                }

            }
        }


        return super.onOptionsItemSelected(item)
    }
}
