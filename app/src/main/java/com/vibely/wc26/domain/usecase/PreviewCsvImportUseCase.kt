package com.vibely.wc26.domain.usecase

import com.vibely.wc26.data.ownership.OwnershipCsvParser
import javax.inject.Inject

/**
 * Parses a CSV string into a quantity count without touching the repository.
 * Used by the import screen to give the user a real-time preview ("X stickers parsed")
 * as they paste/edit the input.
 */
class PreviewCsvImportUseCase
    @Inject
    constructor() {
        operator fun invoke(csv: String): Int = OwnershipCsvParser.parse(csv).quantities.size
    }
