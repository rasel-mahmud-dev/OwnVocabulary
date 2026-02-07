package com.rs.myvocabulary

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.rs.myvocabulary.ui.theme.OwnVocabularyTheme
import com.rs.myvocabulary.composeable.Crawler
import com.rs.myvocabulary.composeable.ScrapedData


class ShareActivity : ComponentActivity() {

    private var receivedLink: String? = null

    val context = this
//    private lateinit var noteViewModel: StateViewModel


    fun handleAddNoteContent(scrapedData: ScrapedData?) {
        if (scrapedData == null) {
            Toast.makeText(context, "Error adding note", Toast.LENGTH_SHORT).show()
            return
        }
        Intent(context, MainActivity::class.java).also {
            it.putExtra("route", "create")
            it.putExtra("content", scrapedData.content)
            it.putExtra("cover", scrapedData.cover)

            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            context.startActivity(it)
            finish()
        }
    }

    private fun copyContent(scrapedData: ScrapedData?) {
        if(scrapedData == null) {
            Toast.makeText(context, "Error copying content", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val shareText = generateShareText(scrapedData)
        val clip = ClipData.newPlainText("Scraped Content", shareText)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this, "Content copied to clipboard", Toast.LENGTH_SHORT).show()
    }


    private fun generateShareText(scrapedData: ScrapedData): String{
       return buildString {
            append(scrapedData.content)
            if (scrapedData.cover.isNotBlank()) {
                append("\n\n${scrapedData.cover}")
            }
        }
    }

    private fun handleShareAsText(scrapedData: ScrapedData?) {
        if (scrapedData == null) {
            Toast.makeText(this, "Error sharing content", Toast.LENGTH_SHORT).show()
            return
        }
        val shareText = generateShareText(scrapedData)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            Intent.setType = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun handleSavePost(scrapedData: ScrapedData?){
        val content = scrapedData?.content ?: ""
        val cover = scrapedData?.cover ?: ""

        if(content.isEmpty()){
            Toast.makeText(context, "Post content required.", Toast.LENGTH_SHORT).show()
            return
        }

//        NoteDatabase.getInstance(context).insertNote(
//            Note(
//                uid = System.currentTimeMillis().toString(),
//                title = makeMarkdownTextToTitle(content),
//                content = content,
//                cover = cover,
//                theme = "default"
//            )
//        )
//        noteViewModel.startNoteSync()
        Toast.makeText(context, "Post has been saved!! ", Toast.LENGTH_SHORT).show()
        handleClose()
    }

    private fun handleClose() {
        (context as Activity).finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val application = applicationContext as Application
//        noteViewModel = StateViewModel(application)

//        receivedLink = handleIncomingIntent(intent)
//        println("receivedLink: $receivedLink")

        val text = intent.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            val application = applicationContext as Application


            OwnVocabularyTheme  {
                Scaffold { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        val scrapedData = remember { mutableStateOf<ScrapedData?>(null) }
                        val scrollState = rememberScrollState()

                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.zIndex(10000f)) {
                                Crawler(text ?: "") {
                                    println("dddddddddddddddd $it")
                                    scrapedData.value = it
                                }
                            }
                        }

//                        Box(modifier = Modifier.fillMaxSize()) {
//                            Row(
//                                modifier = Modifier
//                                    .padding(16.dp)
//                                    .zIndex(10f)
//                                    .align(Alignment.BottomEnd)
//                            ) {
//
//
//                                FloatingActionButton(
//                                    modifier = Modifier
//                                        .padding(horizontal = 5.dp)
//                                        .size(35.dp),
//                                    onClick = {handleClose()}
//                                ) {
//                                    Icon(
//                                        modifier = Modifier.size(18.dp),
//                                        imageVector = Icons.Default.Close,
//                                        contentDescription = "Close"
//                                    )
//                                }
//
//                                FloatingActionButton(
//                                    modifier = Modifier
//                                        .padding(horizontal = 5.dp)
//                                        .size(35.dp),
//                                    onClick = {handleShareAsText(scrapedData.value)}
//                                ) {
//                                    Icon(
//                                        modifier = Modifier.size(18.dp),
//                                        imageVector = Icons.Default.Share,
//                                        contentDescription = "Share"
//                                    )
//                                }
//
//
//                                FloatingActionButton(
//                                    modifier = Modifier
//                                        .padding(horizontal = 5.dp)
//                                        .size(35.dp),
//                                    onClick = { copyContent(scrapedData.value) }
//                                ) {
//                                    Icon(
//                                        modifier = Modifier.size(18.dp),
//                                        imageVector = Icons.Default.CopyAll,
//                                        contentDescription = "Copy"
//                                    )
//                                }
//
//                                FloatingActionButton(
//                                    modifier = Modifier
//                                        .padding(horizontal = 5.dp)
//                                        .size(35.dp),
//                                    onClick = {handleAddNoteContent(scrapedData.value)}
//                                ) {
//                                    Icon(
//                                        modifier = Modifier.size(18.dp),
//                                        imageVector = Icons.Default.Edit,
//                                        contentDescription = "Edit"
//                                    )
//                                }
//
//                                FloatingActionButton(
//                                    modifier = Modifier
//                                        .padding(horizontal = 5.dp)
//                                        .size(35.dp),
//                                    onClick = {handleSavePost(scrapedData.value)}
//                                ) {
//                                    Icon(
//                                        modifier = Modifier.size(18.dp),
//                                        imageVector = Icons.Default.Save,
//                                        contentDescription = "Save"
//                                    )
//                                }
//                            }
//
//
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .verticalScroll(scrollState)
//                                    .background(MaterialTheme.colorScheme.onSecondary)
//                                    .zIndex(1f)
//                            ) {
//
//                                if (!scrapedData.value?.cover.isNullOrEmpty()) {
//                                    Image(
//                                        painter = rememberAsyncImagePainter(scrapedData.value?.cover),
//                                        contentDescription = "Cover Image",
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .heightIn(max = 800.dp),
//                                        contentScale = ContentScale.FillWidth
//                                    )
//                                }
//
//                                Column(modifier = Modifier.padding(8.dp)) {
//                                    Text(
//                                        text = scrapedData.value?.content ?: "",
//                                        fontWeight = FontWeight.Normal,
//                                        modifier = Modifier.padding(vertical = 8.dp)
//                                    )
//                                }
//
//                                Text(scrapedData.value?.cover ?: "--------")
//                            }
//                        }
                    }
                }
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent): String? {

        if (Intent.ACTION_SEND == intent.action && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            text?.let {
                println("incoming text... $it")
                receivedLink = it
            }
        }
        return receivedLink
    }

    override fun onResume() {
        super.onResume()
        intent?.let { handleIncomingIntent(it) }
    }
}
