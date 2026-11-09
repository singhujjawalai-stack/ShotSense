package com.singhujjawalai.shotsense

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * On-device index of what each screenshot contains, derived from OCR.
 * Lives only in the app's private storage; an entry is dropped as soon as
 * its screenshot is trashed. Never syncs, never logs content.
 */
object OcrStore {
    private const val TAG = "OcrStore"
    const val MAX_TEXT_CHARS = 1500
    private const val INDEX_FILE = "shotsense_ocr_index.json"

    data class Entry(
        val path: String,
        val name: String,
        val addedAt: Long,
        val category: String,
        val codes: List<ScreenshotClassifier.Code>,
        val amounts: List<String>,
        val merchant: String?,
        val text: String,
        val clsVersion: Int = 0,
    )

    private fun indexFile(context: Context): File = File(context.filesDir, INDEX_FILE)

    @Synchronized
    fun put(context: Context, entry: Entry) {
        try {
            val root = readRoot(context)
            root.remove(entry.path)
            root.put(entry.path, entryToJson(entry))
            writeRoot(context, root)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist OCR entry for ${entry.name}", e)
        }
    }

    @Synchronized
    fun putAll(context: Context, entries: Collection<Entry>) {
        try {
            val root = readRoot(context)
            for (entry in entries) {
                root.remove(entry.path)
                root.put(entry.path, entryToJson(entry))
            }
            writeRoot(context, root)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist ${entries.size} OCR entries", e)
        }
    }

    @Synchronized
    fun remove(context: Context, path: String) {
        try {
            val root = readRoot(context)
            if (root.has(path)) {
                root.remove(path)
                writeRoot(context, root)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove OCR entry for $path", e)
        }
    }

    @Synchronized
    fun all(context: Context): Map<String, Entry> {
        return try {
            val root = readRoot(context)
            val out = mutableMapOf<String, Entry>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                entryFromJson(key, root.optJSONObject(key) ?: continue)?.let { out[key] = it }
            }
            out
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read OCR index", e)
            emptyMap()
        }
    }

    @Synchronized
    fun toJson(context: Context): String = readRoot(context).toString()

    private fun readRoot(context: Context): JSONObject {
        val file = indexFile(context)
        if (!file.exists()) return JSONObject()
        return try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            Log.w(TAG, "OCR index unreadable, starting fresh", e)
            JSONObject()
        }
    }

    private fun writeRoot(context: Context, root: JSONObject) {
        val file = indexFile(context)
        val tmp = File(context.filesDir, "$INDEX_FILE.tmp")
        tmp.writeText(root.toString())
        if (!tmp.renameTo(file)) {
            file.writeText(root.toString())
            tmp.delete()
        }
    }

    private fun entryToJson(entry: Entry): JSONObject = JSONObject()
        .put("name", entry.name)
        .put("addedAt", entry.addedAt)
        .put("category", entry.category)
        .put("merchant", entry.merchant ?: "")
        .put("text", entry.text)
        .put("clsVersion", entry.clsVersion)
        .put("amounts", JSONArray(entry.amounts))
        .put("codes", JSONArray().apply {
            for (code in entry.codes) {
                put(JSONObject().put("type", code.type).put("value", code.value))
            }
        })

    private fun entryFromJson(path: String, obj: JSONObject): Entry? = try {
        val codesArr = obj.optJSONArray("codes")
        val amountsArr = obj.optJSONArray("amounts")
        Entry(
            path = path,
            name = obj.optString("name"),
            addedAt = obj.optLong("addedAt"),
            category = obj.optString("category"),
            codes = (0 until (codesArr?.length() ?: 0)).map { i ->
                val c = codesArr!!.getJSONObject(i)
                ScreenshotClassifier.Code(c.optString("type"), c.optString("value"))
            },
            amounts = (0 until (amountsArr?.length() ?: 0)).map { i ->
                amountsArr!!.getString(i)
            },
            merchant = obj.optString("merchant").ifBlank { null },
            text = obj.optString("text"),
            clsVersion = obj.optInt("clsVersion", 0),
        )
    } catch (e: Exception) {
        Log.w(TAG, "Skipping corrupt OCR entry $path", e)
        null
    }
}
