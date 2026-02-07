package com.rs.myvocabulary.utils

import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rs.myvocabulary.database.Label
import com.rs.myvocabulary.database.Tag
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.database.WordDatabase
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupUtils {

    fun createDatabaseBackup(context: Context): File? {
        val db = WordDatabase.getInstance(context)
        val exportDir = File(context.cacheDir, "db_export")
        if (exportDir.exists()) {
            exportDir.deleteRecursively()
        }
        exportDir.mkdirs()

        val gson = GsonBuilder().setPrettyPrinting().create()

//        try {
//            // 1. Export Posts
//            val posts = db.getAllPosts()
//            File(exportDir, "posts.json").writeText(gson.toJson(posts))
//
//            // 2. Export Comments
//            val comments = db.getAllComments()
//            File(exportDir, "comments.json").writeText(gson.toJson(comments))
//
//            // 3. Export Tags
//            val tags = db.getAllTags()
//            File(exportDir, "tags.json").writeText(gson.toJson(tags))
//
//            // 4. Export Categories
//            val categories = db.getAllCategories()
//            File(exportDir, "categories.json").writeText(gson.toJson(categories))
//
//            // 5. Zip the JSON files into a data folder
//            val backupDir =
//                    File(Environment.getExternalStorageDirectory(), "learn_media/app_backup")
//            if (!backupDir.exists()) backupDir.mkdirs()
//
//            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
//            val zipFileName = "backup_${sdf.format(Date())}.zip"
//            val zipFile = File(backupDir, zipFileName)
//
//            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
//                // Add JSON files to a 'json' directory in the zip
//                exportDir.listFiles()?.forEach { file ->
//                    val entryName = "json/${file.name}"
//                    zos.putNextEntry(ZipEntry(entryName))
//                    FileInputStream(file).use { fis -> BufferedInputStream(fis).copyTo(zos) }
//                    zos.closeEntry()
//                }
//
//                // Now add assets as a zip within this zip?
//                // "in another dir all assets as zip format"
//                // Let's create the assets zip first
//                val assetsZip = createAssetsBackup(context)
//                if (assetsZip != null) {
//                    zos.putNextEntry(ZipEntry("assets/${assetsZip.name}"))
//                    FileInputStream(assetsZip).use { fis -> BufferedInputStream(fis).copyTo(zos) }
//                    zos.closeEntry()
//                    assetsZip.delete() // Delete the intermediate zip
//                }
//            }
//
//            // Cleanup
//            exportDir.deleteRecursively()
//
//            return zipFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }

        return  null
    }

    fun createAssetsBackup(context: Context): File? {
        try {
            // Assuming LocalAssetManager stores files in "learn_media" subdirectory of external
            // storage root
            val assetsDir = File(Environment.getExternalStorageDirectory(), "learn_media")
            if (!assetsDir.exists() || assetsDir.listFiles()?.isEmpty() == true) {
                return null
            }

            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val zipFile = File(backupDir, "assets_backup.zip")
            zipDirectory(assetsDir, zipFile)

            return zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun zipDirectory(sourceDir: File, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            sourceDir.walkTopDown().onEnter { it.name != "app_backup" }.forEach { file ->
                if (file.isFile) {
                    val entryName = sourceDir.toPath().relativize(file.toPath()).toString()
                    zos.putNextEntry(ZipEntry(entryName))
                    FileInputStream(file).use { fis -> BufferedInputStream(fis).copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }

    fun restoreFromBackup(context: Context, backupInputStream: InputStream): Boolean {
        val tempDir = File(context.cacheDir, "restore_temp")
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        try {
            // 1. Unzip the main backup
            unzip(backupInputStream, tempDir)

            val jsonDir = File(tempDir, "json")
            val assetsDir = File(tempDir, "assets")

            if (!jsonDir.exists()) return false

            val gson = GsonBuilder().create()

            // 2. Clear Database
            val db = WordDatabase.getInstance(context)
//            db.clearAllData()

            // 3. Restore Categories
            val categoriesFile = File(jsonDir, "categories.json")
            if (categoriesFile.exists()) {
                val type = object : TypeToken<List<Label>>() {}.type
                val categories: List<Label> = gson.fromJson(categoriesFile.readText(), type)
//                db.insertCategories(categories)
            }

            // 4. Restore Tags
            val tagsFile = File(jsonDir, "tags.json")
            if (tagsFile.exists()) {
                val type = object : TypeToken<List<Tag>>() {}.type
                val tags: List<Tag> = gson.fromJson(tagsFile.readText(), type)
//                db.insertTags(tags)
            }

            // 5. Restore Posts (includes comments)
            val postsFile = File(jsonDir, "posts.json")
            if (postsFile.exists()) {
                val type = object : TypeToken<List<Word>>() {}.type
                val posts: List<Word> = gson.fromJson(postsFile.readText(), type)
//                db.insertPosts(posts)
            }

            // 6. Restore Assets
            val assetsZipFile = assetsDir.listFiles()?.find { it.name.endsWith(".zip") }
            if (assetsZipFile != null) {
                val finalAssetsDir = File(Environment.getExternalStorageDirectory(), "learn_media")
                if (!finalAssetsDir.exists()) finalAssetsDir.mkdirs()
                FileInputStream(assetsZipFile).use { fis -> unzip(fis, finalAssetsDir) }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            tempDir.deleteRecursively()
        }
    }

    fun getBackupFiles(): List<File> {
        val backupDir = File(Environment.getExternalStorageDirectory(), "learn_media/app_backup")
        if (!backupDir.exists()) return emptyList()
        return backupDir
                .listFiles()
                ?.filter { it.isFile && it.extension == "zip" }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
    }

    fun deleteBackupFile(file: File): Boolean {
        return if (file.exists()) file.delete() else false
    }

    fun restoreFromFile(context: Context, file: File): Boolean {
        return try {
            FileInputStream(file).use { fis -> restoreFromBackup(context, fis) }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun unzip(inputStream: InputStream, targetDir: File) {
        ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    BufferedOutputStream(FileOutputStream(file)).use { bos -> zis.copyTo(bos) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
