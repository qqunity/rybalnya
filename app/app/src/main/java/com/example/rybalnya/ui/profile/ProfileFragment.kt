package com.example.rybalnya.ui.profile

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.adapters.BookmarksAdapter
import com.example.rybalnya.adapters.PhotocardAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.PostRecieve
import com.example.rybalnya.api.UserRecieve
import com.example.rybalnya.models.BookmarkModel
import com.example.rybalnya.models.PostModel
import com.example.rybalnya.utils.UserManager
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File


private var USER_EMAIL = ""
private var USER_TKN = ""

class ProfileFragment : Fragment() {

    //    TODO("add location recycler view")
    private lateinit var recyclerViewPictures: RecyclerView
    private lateinit var recyclerViewBookmarks: RecyclerView
    private lateinit var photocardAdapter: PhotocardAdapter
    private lateinit var bookmarksAdapter: BookmarksAdapter
    private lateinit var postList: ArrayList<PostModel>
    private lateinit var bookmarksList: ArrayList<BookmarkModel>
    private lateinit var userManager: UserManager
    private lateinit var postRecieve: ArrayList<PostRecieve>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userManager = UserManager(this.requireActivity())
        recyclerViewPictures = view.findViewById(R.id.recycler_view_pictures)
        recyclerViewPictures.setHasFixedSize(true)
        val mLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewPictures.layoutManager = mLayoutManager
        val itemDecoration = PostOffsetDecoration(this.requireActivity(), R.dimen.item_offset)
        recyclerViewPictures.addItemDecoration(itemDecoration)
        postList = ArrayList()
        photocardAdapter = PhotocardAdapter(requireContext(), postList)
        recyclerViewPictures.adapter = photocardAdapter

        recyclerViewBookmarks = view.findViewById(R.id.recycler_view_bookmarks)
        recyclerViewBookmarks.setHasFixedSize(false)
        recyclerViewBookmarks.setItemViewCacheSize(100)
        val myLayoutManager: LinearLayoutManager = LinearLayoutManager(context)
        recyclerViewBookmarks.layoutManager = myLayoutManager
        bookmarksList = ArrayList()
        bookmarksAdapter = BookmarksAdapter(requireContext(), bookmarksList)
        recyclerViewBookmarks.addItemDecoration(
            DividerItemDecoration(
                this.requireActivity(),
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerViewBookmarks.adapter = bookmarksAdapter

        bookmarksList.add(BookmarkModel(55.635762, 37.720861, null, 0, null, null, 4, "435"))
        bookmarksList.add(
            BookmarkModel(
                55.632020,
                37.714247,
                "Борисовские пруды",
                0,
                null,
                null,
                4,
                "435"
            )
        )
        bookmarksList.add(
            BookmarkModel(
                55.555361,
                37.652200,
                "Булатниковский пруд",
                0,
                null,
                "Тут рыбка, рыбка, хорошая рыбка, нигде не найдешь лучше. Все, все приездайте сюда, порыбачим вместе, скопируйте координаты, нажав на кнопочку и просто найдите это место в картах. Я буду вас ждать.",
                4,
                "435"
            )
        )

        bookmarksAdapter.notifyDataSetChanged()

        my_bookmarks.setOnClickListener {
            recyclerViewPictures.visibility = View.GONE
            recyclerViewBookmarks.visibility = View.VISIBLE
        }

        my_pictures.setOnClickListener {
            recyclerViewBookmarks.visibility = View.GONE
            recyclerViewPictures.visibility = View.VISIBLE
        }


        topAppBar.inflateMenu(R.menu.current_user_profile_menu)

        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logoutItem -> {
                    Log.i("logoutItem", "Hello there")
                    userManager = UserManager(this.requireActivity())
                    GlobalScope.launch(Dispatchers.IO) {
                        userManager.storeUser(false, "")
                        userManager.editBio("")
                        userManager.editNick("")
                        userManager.editFullName("")
                        GlobalScope.launch(Dispatchers.Main) {
                            val file =
                                File(this@ProfileFragment.requireActivity().filesDir.path.toString() + "/avatar.png")
                            val deleted: Boolean = file.delete()
                        }
                    }
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    true
                }
                R.id.editItem -> {
                    Log.i("editItem", "Hello there")
                    val intent = Intent(activity, SettingsActivity::class.java)
                    this.startActivity(intent)
                    true
                }
                else -> {
                    super.onOptionsItemSelected(it)
                }
            }
        }
        updateViews()

        getUserPosts(MainActivity2().getTkn().toString())

        profile_image.setOnClickListener {
            selectImage(this.requireContext())
        }
    }

    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>("Снять новое", "Выбрать из галереи", "Отмена")
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Выберите фото профиля")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Снять новое" -> {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePicture, 0)
                }
                options[item] == "Выбрать из галереи" -> {
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)
                }
                options[item] == "Отмена" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    profile_image.setImageBitmap(selectedImage)
                    val bos = ByteArrayOutputStream()
                    selectedImage?.compress(Bitmap.CompressFormat.PNG, 100, bos)
                    context?.openFileOutput("avatar.png", Context.MODE_PRIVATE).use {
                        it?.write(bos.toByteArray())
                        try {
                            updateUserAvatar(
                                USER_TKN,
                                USER_EMAIL,
                                Base64.encodeToString(bos.toByteArray(), 0)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage: Uri? = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        try {
                            val ins =
                                this.requireActivity().contentResolver.openInputStream(selectedImage)
                            val bitmap = BitmapFactory.decodeStream(ins)
                            val bos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                            context?.openFileOutput("avatar.png", Context.MODE_PRIVATE).use {
                                it?.write(bos.toByteArray())
                                try {
                                    updateUserAvatar(
                                        USER_TKN,
                                        USER_EMAIL,
                                        Base64.encodeToString(bos.toByteArray(), 0)
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        val cursor: Cursor? = this.requireContext().contentResolver.query(
                            selectedImage,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath: String = cursor.getString(columnIndex)
                            profile_image.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                            cursor.close()
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        updateViews()
        super.onResume()
    }


    private fun updateUserAvatar(
        token: String,
        mail: String,
        image: String
    ) {
        val user = UserRecieve(null, null, mail, null, image, null, null)
        Log.i("userJSON", user.toString())
        var api: ApiRequests? = null
        try {
            api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)
        } catch (e: Exception) {
            Log.i("ApiRequest", e.toString())
        }
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = api?.updateUser(user, token)?.awaitResponse()
                Log.i("updateUserAvatar", response?.body()?.Action.toString())
                Toast.makeText(
                    this@ProfileFragment.requireActivity(),
                    response?.body()?.Action.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.i("updateUserAvatar", e.toString())
            }
        }
    }

    private fun updateViews() {

        userManager = UserManager(this.requireActivity())
        if (nickname != null) {
            userManager.userNickFlow.asLiveData().observe(this.requireActivity()) { s ->
                if (s == "") {
                    userManager.userMailFlow.asLiveData().observe(viewLifecycleOwner) {
                        nickname?.text = it
                    }
                } else {
                    nickname?.text = s
                }
            }
        }

        if (expandable_text != null) {
            userManager.userBioFlow.asLiveData().observe(this.requireActivity()) {
                expandable_text?.text = it
                expand_text_view?.text = it
            }
        }

        if (fullname != null) {
            userManager.userFullNameFlow.asLiveData().observe(this.requireActivity()) {
                fullname?.text = it
            }
        }
        userManager.userMailFlow.asLiveData().observe(this.requireActivity()) {
            USER_EMAIL = it
        }
        userManager.userTokenFlow.asLiveData().observe(this.requireActivity()) {
            USER_TKN = it
        }

        if (File(this.requireActivity().filesDir.path.toString() + "/avatar.png").exists()) {
            profile_image.setImageBitmap(BitmapFactory.decodeFile((this.requireActivity().filesDir.path.toString() + "/avatar.png")))
        } else {
            profile_image.setImageResource(R.mipmap.ic_launcher)
        }
    }

    private fun getUserPosts(token: String) {
        var api: ApiRequests? = null
        try {
            api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)
        } catch (e: Exception) {
            Log.i("ApiRequest", e.toString())
        }
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = api?.getCurrentUserPosts(token)?.awaitResponse()

                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getPosts", "Successful")
                    postRecieve = ArrayList()
                    if (response.body()?.Posts != null) {
                        postRecieve = response.body()!!.Posts
                    } else {
                        postRecieve.clear()
                        Toast.makeText(
                            this@ProfileFragment.requireActivity(),
                            "Посты не найдены",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    val res = ArrayList<PostModel>()

                    for (post in postRecieve) {
                        res.add(
                            PostModel(
                                post.ID!!,
                                post.Image!!,
                                post.Description,
                                post.UserID!!,
                                post.CreatedAt!!
                            )
                        )
                    }

                    postList.clear()
                    postList.addAll(res)
                    postList.reverse()

                    photocardAdapter.notifyDataSetChanged()
/*                    swipeRefreshLayout.isRefreshing = false
                    isRefreshing = false
                    mLayoutManager.scrollToPosition(postList.size - 1)*/

                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getPosts", "Something went wrong")
                }
            } catch (e: Exception) {
                Log.i("getPosts", e.toString())
            }
        }
    }

}

class PostOffsetDecoration(private val mItemOffset: Int) : ItemDecoration() {
    constructor(
        @NonNull context: Context,
        @DimenRes itemOffsetId: Int
    ) : this(context.resources.getDimensionPixelSize(itemOffsetId))

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lps = view.layoutParams as GridLayoutManager.LayoutParams
        outRect.top = mItemOffset / 2
        outRect.bottom = mItemOffset
        when (lps.spanIndex) {
            0 -> {
                outRect.left = mItemOffset * 2
                outRect.right = mItemOffset / 2
            }
            2 -> {
                outRect.left = mItemOffset / 2
                outRect.right = mItemOffset
            }
            1 -> {
                outRect.left = mItemOffset / 2
                outRect.right = mItemOffset / 2
            }
        }
    }
}

