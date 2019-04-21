package com.indieteam.cameratranslate.ui

import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import com.indieteam.cameratranslate.R
import com.indieteam.cameratranslate.collection.TranslateCollection
import com.indieteam.cameratranslate.process.DatabaseQuery
import kotlinx.android.synthetic.main.activity_saved.*

class SavedActivity : AppCompatActivity() {

    lateinit var database: DatabaseQuery
    private val data = ArrayList<TranslateCollection>()
    private val dataTemp = ArrayList<TranslateCollection>()
    private lateinit var recyclerViewAdpater: RecyclerViewAdpater
    private var isSearch = false

    private var posSwiped = 0
    private var lastSwipe = -1
    private var moving = false
    private var deleteButtonVisible = false

    private lateinit var alertDialogBuilder: AlertDialog.Builder

    private val swipeController = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {
        override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
            val position = p0.layoutPosition

            if (lastSwipe != -1 && lastSwipe != position)
                recyclerViewAdpater.notifyItemChanged(lastSwipe)

            lastSwipe = position
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            posSwiped = viewHolder.adapterPosition


            Log.d("dX", dX.toString())
            Log.d("Item postion", posSwiped.toString())
            val view = viewHolder.itemView

            val paint = Paint()
            paint.color = resources.getColor(R.color.colorAccent)
            paint.textSize = 50f
            paint.isAntiAlias = true

            val deleteButtonLeft = view.right - (view.right / 5f)
            val deleteButtonTop = view.top.toFloat()
            val deleteButtonRight = view.right.toFloat() - view.paddingRight
            val deleteButtonBottom = view.bottom.toFloat()

            Log.d("Delete Button Left X", deleteButtonLeft.toString())

            val radius = 15f

            val deleteButtonDelete = RectF(deleteButtonLeft, deleteButtonTop, deleteButtonRight, deleteButtonBottom)
            c.drawRoundRect(deleteButtonDelete, radius, radius, paint)
            paint.color = resources.getColor(R.color.colorWhite)

            val textButton = "Xóa"

            val rect = Rect()
            paint.getTextBounds(textButton, 0, textButton.length, rect)

            c.drawText(
                    "Xóa",
                    deleteButtonDelete.centerX() - rect.width() / 2f,
                    deleteButtonDelete.centerY() + rect.height() / 2f,
                    paint
            )

            // this dX run from 0 to `-xxxx` width of screen, dX of item change like this dX

            if (dX <= - deleteButtonLeft) {
                deleteButtonVisible = true
                moving = false
            } else
            {
                deleteButtonVisible = false
                moving = true
            }

            if (dX == 0.0f)
                moving = false

            Log.d("Moving", "$moving")

            Log.d("Button Visible", deleteButtonVisible.toString())

            if (deleteButtonVisible)
                clickDeleteButtonListener(recyclerView, viewHolder, posSwiped)

            super.onChildDraw(c, recyclerView, viewHolder, dX / 5f, dY, actionState, isCurrentlyActive)
        }

    }

    private fun clickDeleteButtonListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, posSwiped: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(posSwiped)
        val item = viewHolder?.itemView

        item?.let {
            recyclerView.setOnTouchListener { v, event ->
                Log.d("X click", event.x.toString())
                Log.d("X item end", "${item.x + item.width}")
                Log.d("Y click", event.y.toString())
                Log.d("Y item start", item.y.toString())
                Log.d("Y item end", "${item.y + item.height}")
                Log.d("Button Visible", deleteButtonVisible.toString())


                // If pos X,Y clicked in pos of rect delete (item swiped)
                if(event.action == MotionEvent.ACTION_UP && event.y > item.y && event.y < item.y + item.height
                        && event.x > item.x + item.width && !moving){
                    if (deleteButtonVisible) {
                        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
                        deleteItem(posSwiped)
                        deleteButtonVisible = false
                    }
                }
                false
            }
        }
    }

    private fun deleteItem(pos: Int){
        val delete = database.delete(data[pos].word_id)
        if(delete) {
            var posTemp = -1
            for (i in dataTemp) {
                posTemp++
                if (i.word_id == data[pos].word_id) {
                    break
                }
            }

            data.removeAt(pos)
            dataTemp.removeAt(posTemp)
        }

        recyclerViewAdpater.notifyItemRemoved(pos)
    }
    
    private val dialogClickListener = object : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    // Yes
                    val delete = database.deleteAll()
                    if(delete) {
                        data.clear()
                        dataTemp.clear()
                        recyclerViewAdpater.notifyDataSetChanged()
                        title = "Đã lưu"
                        Toast.makeText(this@SavedActivity, "Đã xoá toàn bộ", Toast.LENGTH_SHORT).show()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    // No
                }
            }
        }

    }

    private fun init() {
        database = DatabaseQuery(this)
        data.addAll(database.readALL())
        dataTemp.addAll(data)
        recyclerViewAdpater = RecyclerViewAdpater(this, data)

        Log.d("data size", data.size.toString())

        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = recyclerViewAdpater
        ItemTouchHelper(swipeController).attachToRecyclerView(recycler_view)

        alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Xóa mọi thứ đã lưu?")
                .setPositiveButton("Xóa hết", dialogClickListener)
                .setNegativeButton("Thoát", dialogClickListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_search, menu)
        menu?.let {
            val searchItem = it.findItem(R.id.local_search)
            val searchView = searchItem.actionView as SearchView
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.queryHint = "Tìm kiếm"

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    // Clear text
                    searchView.isIconified = true
                    // Close
                    searchView.onActionViewCollapsed()

                    val keyWord = p0

                    keyWord?.let {
                        title = "Tìm kiếm cho \"" + keyWord + "\""
                        isSearch = true
                        search(keyWord)
                    }

                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    return true
                }

            })
        }
        return true
    }


    private fun search(keyWord: String) {
        data.clear()
        for (i in dataTemp) {
            if (i.word_raw.toLowerCase().trim().indexOf(keyWord.toLowerCase().trim()) > -1)
                data.add(TranslateCollection(i.word_id, i.word_raw, i.word_translate))
        }
        recyclerViewAdpater.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.deleteAll -> {
                alertDialogBuilder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isSearch) {
            isSearch = false
            data.clear()
            data.addAll(dataTemp)
            recyclerViewAdpater.notifyDataSetChanged()
            title = "Đã lưu"
        } else
            finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)
        setSupportActionBar(toolbar)
        title = "Đã lưu"
        init()
    }
}
