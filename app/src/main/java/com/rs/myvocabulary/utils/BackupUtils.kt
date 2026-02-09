package com.rs.myvocabulary.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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

        try {
            // 2. Export Words (includes embedded comments, categories)
            val words = db.getAllWords()
            File(exportDir, "posts.json").writeText(gson.toJson(words))

            // 4. Zip the JSON files into a data folder
            val backupDir = context.getExternalFilesDir("backups")
            if (backupDir == null || (!backupDir.exists() && !backupDir.mkdirs())) {
                return null
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val zipFileName = "backup_${sdf.format(Date())}.zip"
            val zipFile = File(backupDir, zipFileName)

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                // Add JSON files to a 'json' directory in the zip
                exportDir.listFiles()?.forEach { file ->
                    val entryName = "json/${file.name}"
                    zos.putNextEntry(ZipEntry(entryName))
                    FileInputStream(file).use { fis -> BufferedInputStream(fis).copyTo(zos) }
                    zos.closeEntry()
                }

                // Add assets as a zip within this zip
                val assetsZip = createAssetsBackup(context)
                if (assetsZip != null) {
                    zos.putNextEntry(ZipEntry("assets/${assetsZip.name}"))
                    FileInputStream(assetsZip).use { fis -> BufferedInputStream(fis).copyTo(zos) }
                    zos.closeEntry()
                    assetsZip.delete() // Delete the intermediate zip
                }
            }

            // Cleanup
            exportDir.deleteRecursively()

            return zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun createAssetsBackup(context: Context): File? {
        try {
            val assetsDir = context.getExternalFilesDir("media")
            if (assetsDir == null || !assetsDir.exists() || assetsDir.listFiles()?.isEmpty() == true
            ) {
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
            val db = WordDatabase.getInstance(context)

            // 2. Clear Database before restoration
            db.clearAllData()

            // 5. Restore Words (includes embedded comments, categories)
            val postsFile = File(jsonDir, "posts.json")
            if (postsFile.exists()) {
                val type = object : TypeToken<List<Word>>() {}.type
                val posts: List<Word> = gson.fromJson(postsFile.readText(), type)
                db.insertWords(posts)
            }

            // 6. Restore Assets
            val assetsZipFile = assetsDir.listFiles()?.find { it.name.endsWith(".zip") }
            if (assetsZipFile != null) {
                val finalAssetsDir = context.getExternalFilesDir("media")
                if (finalAssetsDir != null) {
                    if (!finalAssetsDir.exists()) finalAssetsDir.mkdirs()
                    FileInputStream(assetsZipFile).use { fis -> unzip(fis, finalAssetsDir) }
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            tempDir.deleteRecursively()
        }
    }

    fun getBackupFiles(context: Context): List<File> {
        val backupDir = context.getExternalFilesDir("backups")
        if (backupDir == null || !backupDir.exists()) return emptyList()
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

    fun copyFileToPublicDownloads(context: Context, sourceFile: File, fileName: String): Boolean {
        return try {
            val contentValues =
                    ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(
                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                    Environment.DIRECTORY_DOWNLOADS + "/VocabBook"
                            )
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                    }

            val resolver = context.contentResolver
            val collection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Files.getContentUri("external")
                    }

            val uri = resolver.insert(collection, contentValues) ?: return false

            resolver.openOutputStream(uri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream -> inputStream.copyTo(outputStream) }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
