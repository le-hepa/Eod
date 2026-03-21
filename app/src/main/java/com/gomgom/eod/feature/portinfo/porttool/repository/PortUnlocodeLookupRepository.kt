package com.gomgom.eod.feature.portinfo.porttool.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class PortUnlocodeEntry(
    val countryCode: String,
    val countryName: String,
    val locode: String,
    val portName: String
)

class PortUnlocodeLookupRepository(
    private val context: Context
) {
    private val mutex = Mutex()
    @Volatile
    private var entries: List<PortUnlocodeEntry>? = null

    suspend fun searchCountries(query: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalized = query.normalizeLookup()
        loadEntries()
            .asSequence()
            .filter {
                normalized.isBlank() ||
                    it.countryName.normalizeLookup().contains(normalized) ||
                    it.countryCode.normalizeLookup().contains(normalized)
            }
            .distinctBy { "${it.countryCode}|${it.countryName}" }
            .take(limit)
            .toList()
    }

    suspend fun searchPorts(query: String, countryHint: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.normalizeLookup()
        val normalizedCountry = countryHint.normalizeLookup()
        loadEntries()
            .asSequence()
            .filter {
                (normalizedCountry.isBlank() ||
                    it.countryName.normalizeLookup().contains(normalizedCountry) ||
                    it.countryCode.normalizeLookup().contains(normalizedCountry)) &&
                    (normalizedQuery.isBlank() ||
                        it.portName.normalizeLookup().contains(normalizedQuery) ||
                        it.locode.normalizeLookup().contains(normalizedQuery))
            }
            .distinctBy { "${it.countryCode}|${it.portName}" }
            .take(limit)
            .toList()
    }

    suspend fun findByUnlocode(code: String): PortUnlocodeEntry? = withContext(Dispatchers.IO) {
        val normalized = code.normalizeLookup()
        loadEntries().firstOrNull {
            it.locode.normalizeLookup() == normalized ||
                it.locode.takeLast(3).normalizeLookup() == normalized
        }
    }

    suspend fun resolve(country: String, port: String): PortUnlocodeEntry? = withContext(Dispatchers.IO) {
        val normalizedCountry = country.normalizeLookup()
        val normalizedPort = port.normalizeLookup()
        loadEntries().firstOrNull {
            (normalizedCountry.isBlank() ||
                it.countryName.normalizeLookup() == normalizedCountry ||
                it.countryCode.normalizeLookup() == normalizedCountry) &&
                (normalizedPort.isBlank() || it.portName.normalizeLookup() == normalizedPort)
        }
    }

    private suspend fun loadEntries(): List<PortUnlocodeEntry> {
        entries?.let { return it }
        return mutex.withLock {
            entries?.let { return@withLock it }
            val loaded = context.assets.open("unlocode_app_ready.csv").bufferedReader().useLines { lines ->
                lines.drop(1).mapNotNull { line ->
                    val cols = parseCsvLine(line)
                    if (cols.size < 6) return@mapNotNull null
                    PortUnlocodeEntry(
                        countryCode = cols[1],
                        countryName = cols[2],
                        locode = cols[4],
                        portName = cols[5]
                    )
                }.toList()
            }
            entries = loaded
            loaded
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result += current.toString()
        return result
    }
}

private fun String.normalizeLookup(): String = lowercase().replace(" ", "").trim()
