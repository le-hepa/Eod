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
    private data class IndexedPortUnlocodeEntry(
        val entry: PortUnlocodeEntry,
        val normalizedCountryCode: String,
        val normalizedCountryName: String,
        val normalizedLocode: String,
        val normalizedPortName: String,
        val normalizedPortCode: String
    )

    private data class PortUnlocodeIndex(
        val entries: List<PortUnlocodeEntry>,
        val countryEntries: List<IndexedPortUnlocodeEntry>,
        val indexedEntries: List<IndexedPortUnlocodeEntry>,
        val countryMap: Map<String, List<IndexedPortUnlocodeEntry>>,
        val portMap: Map<String, List<IndexedPortUnlocodeEntry>>,
        val codeMap: Map<String, IndexedPortUnlocodeEntry>
    )

    private val mutex = Mutex()
    @Volatile
    private var index: PortUnlocodeIndex? = null

    suspend fun searchCountries(query: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalized = query.normalizeLookup()
        val unlocodeIndex = loadIndex()
        unlocodeIndex.countryEntries
            .asSequence()
            .mapNotNull { indexed ->
                val score = countryMatchScore(indexed, normalized) ?: return@mapNotNull null
                indexed to score
            }
            .sortedWith(
                compareBy<Pair<IndexedPortUnlocodeEntry, CountryMatchScore>>(
                    { it.second.countryRank },
                    { it.second.startIndex },
                    { it.second.lengthScore },
                    { it.first.entry.countryName }
                )
            )
            .take(limit)
            .map { it.first.entry }
            .toList()
    }

    suspend fun searchPorts(query: String, countryHint: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.normalizeLookup()
        val normalizedCountry = countryHint.normalizeLookup()
        val unlocodeIndex = loadIndex()
        unlocodeIndex.indexedEntries
            .asSequence()
            .filter {
                (normalizedCountry.isBlank() ||
                    it.normalizedCountryName.contains(normalizedCountry) ||
                    it.normalizedCountryCode.contains(normalizedCountry)) &&
                    (normalizedQuery.isBlank() ||
                        it.normalizedPortName.contains(normalizedQuery) ||
                        it.normalizedLocode.contains(normalizedQuery))
            }
            .distinctBy { "${it.entry.countryCode}|${it.entry.portName}" }
            .take(limit)
            .map { it.entry }
            .toList()
    }

    suspend fun searchPortsByName(query: String, countryHint: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.normalizeLookup()
        val normalizedCountry = countryHint.normalizeLookup()
        val unlocodeIndex = loadIndex()
        unlocodeIndex.indexedEntries
            .asSequence()
            .mapNotNull { indexed ->
                val countryMatched = normalizedCountry.isBlank() ||
                    indexed.normalizedCountryName.contains(normalizedCountry) ||
                    indexed.normalizedCountryCode.contains(normalizedCountry)
                if (!countryMatched) return@mapNotNull null

                val score = portNameMatchScore(indexed, normalizedQuery, normalizedCountry) ?: return@mapNotNull null
                indexed to score
            }
            .sortedWith(
                compareBy<Pair<IndexedPortUnlocodeEntry, PortNameMatchScore>>(
                    { it.second.portRank },
                    { it.second.startIndex },
                    { it.second.countryPenalty },
                    { it.second.lengthScore },
                    { it.first.entry.portName },
                    { it.first.entry.countryName }
                )
            )
            .distinctBy { "${it.first.entry.countryCode}|${it.first.entry.portName}" }
            .take(limit)
            .map { it.first.entry }
            .toList()
    }

    suspend fun searchCountriesByCode(query: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalized = query.normalizeLookup()
        val unlocodeIndex = loadIndex()
        unlocodeIndex.countryEntries
            .asSequence()
            .filter { normalized.isBlank() || it.normalizedCountryCode.startsWith(normalized) }
            .take(limit)
            .map { it.entry }
            .toList()
    }

    suspend fun searchByCode(query: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val normalized = query.normalizeLookup()
        val unlocodeIndex = loadIndex()
        unlocodeIndex.indexedEntries
            .asSequence()
            .filter {
                normalized.isBlank() ||
                    it.normalizedLocode == normalized ||
                    it.normalizedLocode.startsWith(normalized) ||
                    it.normalizedPortCode == normalized ||
                    it.normalizedPortCode.startsWith(normalized)
            }
            .sortedWith(
                compareBy<IndexedPortUnlocodeEntry> {
                    when {
                        it.normalizedLocode == normalized || it.normalizedPortCode == normalized -> 0
                        it.normalizedLocode.startsWith(normalized) || it.normalizedPortCode.startsWith(normalized) -> 1
                        else -> 2
                    }
                }.thenBy { codeDistanceScore(it, normalized) }
                    .thenBy { it.entry.countryName }
                    .thenBy { it.entry.portName }
            )
            .distinctBy { "${it.entry.countryCode}|${it.entry.locode}|${it.entry.portName}" }
            .take(limit)
            .map { it.entry }
            .toList()
    }

    suspend fun searchGeneral(query: String, limit: Int = 7): List<PortUnlocodeEntry> = withContext(Dispatchers.IO) {
        val tokens = query.normalizeLookupTokens()
        val unlocodeIndex = loadIndex()
        if (tokens.isEmpty()) return@withContext emptyList()

        unlocodeIndex.indexedEntries
            .asSequence()
            .mapNotNull { indexed ->
                val score = generalMatchScore(indexed, tokens) ?: return@mapNotNull null
                indexed to score
            }
            .sortedWith(
                compareBy<Pair<IndexedPortUnlocodeEntry, GeneralMatchScore>>(
                    { it.second.totalRank },
                    { it.second.worstRank },
                    { it.second.distributedMatchPenalty },
                    { it.second.exactPenalty },
                    { it.second.prefixPenalty },
                    { it.second.lengthScore },
                    { it.first.entry.countryName },
                    { it.first.entry.portName }
                )
            )
            .distinctBy { "${it.first.entry.countryCode}|${it.first.entry.portName}" }
            .take(limit)
            .map { it.first.entry }
            .toList()
    }

    private fun codeDistanceScore(entry: IndexedPortUnlocodeEntry, query: String): Int {
        if (query.isBlank()) return Int.MAX_VALUE
        val locodeDistance = kotlin.math.abs(entry.normalizedLocode.length - query.length)
        val portCodeDistance = kotlin.math.abs(entry.normalizedPortCode.length - query.length)
        return minOf(locodeDistance, portCodeDistance)
    }

    private fun generalMatchScore(
        indexed: IndexedPortUnlocodeEntry,
        tokens: List<String>
    ): GeneralMatchScore? {
        var totalRank = 0
        var worstRank = Int.MIN_VALUE
        var exactMatches = 0
        var prefixMatches = 0
        var matchedCountry = false
        var matchedPort = false
        tokens.forEach { token ->
            val countryRank = when {
                indexed.normalizedCountryName == token -> 0
                indexed.normalizedCountryName.startsWith(token) -> 1
                indexed.normalizedCountryName.wordStarts().any { it.startsWith(token) } -> 2
                indexed.normalizedCountryName.contains(token) -> 3
                else -> null
            }
            val portRank = when {
                indexed.normalizedPortName == token -> 0
                indexed.normalizedPortName.startsWith(token) -> 1
                indexed.normalizedPortName.wordStarts().any { it.startsWith(token) } -> 2
                indexed.normalizedPortName.contains(token) -> 3
                else -> null
            }
            val tokenRank = listOfNotNull(countryRank, portRank).minOrNull() ?: return null
            totalRank += tokenRank
            worstRank = maxOf(worstRank, tokenRank)

            when {
                countryRank != null && (portRank == null || countryRank <= portRank) -> {
                    matchedCountry = true
                    if (countryRank == 0) exactMatches++
                    if (countryRank <= 1) prefixMatches++
                }
                portRank != null -> {
                    matchedPort = true
                    if (portRank == 0) exactMatches++
                    if (portRank <= 1) prefixMatches++
                }
            }
        }

        val distributedMatchPenalty = when {
            tokens.size <= 1 -> 0
            matchedCountry && matchedPort -> 0
            else -> 1
        }

        return GeneralMatchScore(
            totalRank = totalRank,
            worstRank = worstRank,
            distributedMatchPenalty = distributedMatchPenalty,
            exactPenalty = tokens.size - exactMatches,
            prefixPenalty = tokens.size - prefixMatches,
            lengthScore = indexed.entry.countryName.length + indexed.entry.portName.length
        )
    }

    private data class GeneralMatchScore(
        val totalRank: Int,
        val worstRank: Int,
        val distributedMatchPenalty: Int,
        val exactPenalty: Int,
        val prefixPenalty: Int,
        val lengthScore: Int
    )

    private fun portNameMatchScore(
        indexed: IndexedPortUnlocodeEntry,
        query: String,
        countryHint: String
    ): PortNameMatchScore? {
        if (query.isBlank()) {
            return PortNameMatchScore(
                portRank = 4,
                startIndex = Int.MAX_VALUE,
                countryPenalty = if (countryHint.isBlank()) 0 else 1,
                lengthScore = indexed.entry.portName.length
            )
        }

        val portRank = when {
            indexed.normalizedPortName == query -> 0
            indexed.normalizedPortName.startsWith(query) -> 1
            indexed.normalizedPortName.wordStarts().any { it.startsWith(query) } -> 2
            indexed.normalizedPortName.contains(query) -> 3
            else -> return null
        }

        val countryPenalty = when {
            countryHint.isBlank() -> 0
            indexed.normalizedCountryName == countryHint || indexed.normalizedCountryCode == countryHint -> 0
            indexed.normalizedCountryName.startsWith(countryHint) || indexed.normalizedCountryCode.startsWith(countryHint) -> 1
            else -> 2
        }

        return PortNameMatchScore(
            portRank = portRank,
            startIndex = indexed.normalizedPortName.indexOf(query).coerceAtLeast(0),
            countryPenalty = countryPenalty,
            lengthScore = indexed.entry.portName.length
        )
    }

    private data class PortNameMatchScore(
        val portRank: Int,
        val startIndex: Int,
        val countryPenalty: Int,
        val lengthScore: Int
    )

    private fun countryMatchScore(
        indexed: IndexedPortUnlocodeEntry,
        query: String
    ): CountryMatchScore? {
        if (query.isBlank()) {
            return CountryMatchScore(
                countryRank = 4,
                startIndex = Int.MAX_VALUE,
                lengthScore = indexed.entry.countryName.length
            )
        }

        val countryRank = when {
            indexed.normalizedCountryName == query || indexed.normalizedCountryCode == query -> 0
            indexed.normalizedCountryName.startsWith(query) || indexed.normalizedCountryCode.startsWith(query) -> 1
            indexed.normalizedCountryName.wordStarts().any { it.startsWith(query) } -> 2
            indexed.normalizedCountryName.contains(query) -> 3
            else -> return null
        }

        val countryStartIndex = listOf(
            indexed.normalizedCountryName.indexOf(query),
            indexed.normalizedCountryCode.indexOf(query)
        ).filter { it >= 0 }.minOrNull() ?: Int.MAX_VALUE

        return CountryMatchScore(
            countryRank = countryRank,
            startIndex = countryStartIndex,
            lengthScore = indexed.entry.countryName.length
        )
    }

    private data class CountryMatchScore(
        val countryRank: Int,
        val startIndex: Int,
        val lengthScore: Int
    )

    suspend fun findByUnlocode(code: String): PortUnlocodeEntry? = withContext(Dispatchers.IO) {
        val normalized = code.normalizeLookup()
        loadIndex().codeMap[normalized]?.entry
    }

    suspend fun resolve(country: String, port: String): PortUnlocodeEntry? = withContext(Dispatchers.IO) {
        val normalizedCountry = country.normalizeLookup()
        val normalizedPort = port.normalizeLookup()
        val unlocodeIndex = loadIndex()
        val countryCandidates = when {
            normalizedCountry.isBlank() -> unlocodeIndex.indexedEntries
            else -> unlocodeIndex.countryMap[normalizedCountry] ?: unlocodeIndex.indexedEntries
        }

        countryCandidates.firstOrNull {
            (normalizedCountry.isBlank() ||
                it.normalizedCountryName == normalizedCountry ||
                it.normalizedCountryCode == normalizedCountry) &&
                (normalizedPort.isBlank() || it.normalizedPortName == normalizedPort)
        }?.entry
    }

    private suspend fun loadIndex(): PortUnlocodeIndex {
        index?.let { return it }
        return mutex.withLock {
            index?.let { return@withLock it }
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
            val indexedEntries = loaded.map { entry ->
                IndexedPortUnlocodeEntry(
                    entry = entry,
                    normalizedCountryCode = entry.countryCode.normalizeLookup(),
                    normalizedCountryName = entry.countryName.normalizeLookup(),
                    normalizedLocode = entry.locode.normalizeLookup(),
                    normalizedPortName = entry.portName.normalizeLookup(),
                    normalizedPortCode = entry.locode.takeLast(3).normalizeLookup()
                )
            }
            val builtIndex = PortUnlocodeIndex(
                entries = loaded,
                countryEntries = indexedEntries.distinctBy {
                    "${it.entry.countryCode}|${it.entry.countryName}"
                },
                indexedEntries = indexedEntries,
                countryMap = indexedEntries
                    .distinctBy { "${it.entry.countryCode}|${it.entry.countryName}" }
                    .flatMap { indexed ->
                        listOf(indexed.normalizedCountryCode, indexed.normalizedCountryName)
                            .filter { it.isNotBlank() }
                            .map { key -> key to indexed }
                    }
                    .groupBy({ it.first }, { it.second }),
                portMap = indexedEntries
                    .flatMap { indexed ->
                        listOf(indexed.normalizedPortName, indexed.normalizedLocode, indexed.normalizedPortCode)
                            .filter { it.isNotBlank() }
                            .map { key -> key to indexed }
                    }
                    .groupBy({ it.first }, { it.second }),
                codeMap = indexedEntries.flatMap { indexed ->
                    listOf(indexed.normalizedLocode, indexed.normalizedPortCode)
                        .filter { it.isNotBlank() }
                        .map { key -> key to indexed }
                }.associateBy({ it.first }, { it.second })
            )
            index = builtIndex
            builtIndex
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

private fun String.normalizeLookup(): String = lowercase().replace(" ", "").replace("-", "").trim()
private fun String.normalizeLookupTokens(): List<String> =
    lowercase()
        .replace("-", " ")
        .split(" ")
        .map { it.replace(" ", "").trim() }
        .filter { it.isNotBlank() }

private fun String.wordStarts(): List<String> =
    lowercase()
        .replace("-", " ")
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotBlank() }
