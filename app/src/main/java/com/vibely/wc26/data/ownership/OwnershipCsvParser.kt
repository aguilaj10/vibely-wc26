package com.vibely.wc26.data.ownership

/**
 * Parses Panini tracker CSV export into a stickerId → quantity map.
 *
 * Expected CSV format (from the Google Spreadsheet export):
 * ```
 * Sticker ID,Player Name,Quantity
 * MEX01,Mexico,1
 * MEX02,Luis Malagón,2
 * ...
 * ```
 *
 * Or simplified format (ID and quantity only):
 * ```
 * MEX01,1
 * MEX02,2
 * ```
 */
internal object OwnershipCsvParser {
    data class ParseResult(
        val quantities: Map<String, Int>,
        val skippedIds: List<String>,
    )

    fun parse(csv: String): ParseResult {
        val quantities = mutableMapOf<String, Int>()
        val skippedIds = mutableListOf<String>()

        csv
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { line ->
                val cells = line.split(",").map { it.trim().trim('"') }
                if (cells.size >= 2) {
                    val id = cells[0].uppercase()
                    val quantity = cells.last().toIntOrNull()
                    if (quantity != null && quantity > 0) {
                        quantities[id] = quantity
                    } else if (quantity != null && quantity <= 0) {
                        skippedIds.add(id)
                    }
                }
            }

        return ParseResult(quantities = quantities, skippedIds = skippedIds)
    }
}
