package com.example.rybalnya.ui.newPost

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.BuildConfig
import com.example.rybalnya.R
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.PostRecieve
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private lateinit var userTkn: String

class PublishActivity : AppCompatActivity() {

    private lateinit var mUri: Uri

    private val selectButton by lazy { findViewById<ImageView>(R.id.select) }
    private val cropLayout by lazy { findViewById<ImageView>(R.id.crop_view) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        userTkn = intent.getStringExtra("User_Token").toString()
        topAppBar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        publish.setOnClickListener {
            publishPost(userTkn)
        }


        selectButton.setOnClickListener {
            chooseImage()
        }

        chooseImage()

    }

    private fun chooseImage() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_message_get_picture))
            .setItems(
                arrayOf(
                    getString(R.string.dialog_message_select_picture),
                    getString(R.string.dialog_message_take_picture)
                )
            ) { dialog, which ->
                when (which) {
                    0 -> {
                        val launcher = registerForActivityResult(GetContent()) { uri: Uri? ->
                            if (uri == null) return@registerForActivityResult
                            startCrop(uri)
                        }
                        launcher.launch("image/*")
                    }
                    1 -> {
                        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val timestamp = format.format(Date())
                        val storageDir: File? =
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        val file = File.createTempFile("PNG_${timestamp}_", ".png", storageDir)
                        val authority = "${BuildConfig.APPLICATION_ID}.fileprovider"
                        mUri = FileProvider.getUriForFile(this, authority, file)
                        val launcher =
                            registerForActivityResult(TakePicture()) { isSuccessful: Boolean ->
                                if (!isSuccessful) return@registerForActivityResult
                                startCrop(mUri)
                            }
                        launcher.launch(mUri)
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun publishPost(token: String) {
        val bos = ByteArrayOutputStream()
        val bitmap = (cropLayout.drawable as BitmapDrawable).bitmap
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val post = PostRecieve(
            description.text.toString(),
            null,
            Base64.encodeToString(bos.toByteArray(), 0),
            null,
            null
        )
        Log.i("userJSON", post.toString())
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
                val response = api?.createPost(post, token)?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 201) {
                    Log.i("publishPost", "Successful")
                    Toast.makeText(
                        this@PublishActivity,
                        response.body()?.Action.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    onBackPressed()
                    finish()
                } else if (response?.code() == 401) {
                    Log.i("publishPost", "Something went wrong")
                    Toast.makeText(
                        this@PublishActivity,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("publishPost", e.toString())
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val dest: String = StringBuilder(UUID.randomUUID().toString() + ".png").toString()

        val ucrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, dest))).withAspectRatio(1f, 1f)

        ucrop.start(this@PublishActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UCrop.REQUEST_CROP) {
            val result = data?.let { UCrop.getOutput(it) }
            if (result != null) {
                cropLayout.setImageURI(result)
                mUri = result
            } else {
                Toast.makeText(this, "Can't retrieve image", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}