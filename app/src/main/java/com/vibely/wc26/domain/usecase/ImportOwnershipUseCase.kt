package com.vibely.wc26.domain.usecase

import com.vibely.wc26.data.ownership.OwnershipCsvParser
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject

class ImportOwnershipUseCase
    @Inject
    constructor(
        private val catalogRepository: CatalogRepository,
        private val ownershipRepository: OwnershipRepository,
    ) {
        data class ImportResult(
            val importedCount: Int,
            val skippedIds: List<String>,
            val unknownIds: List<String>,
        )

        suspend operator fun invoke(csv: String): ImportResult {
            val parseResult = OwnershipCsvParser.parse(csv)
            val catalog = catalogRepository.load()
            val validIds = catalog.stickers.map { it.id }.toSet()

            val valid = parseResult.quantities.filterKeys { it in validIds }
            val unknownIds =
                parseResult.quantities.keys
                    .filter { it !in validIds }
                    .toList()

            ownershipRepository.importAll(valid)

            return ImportResult(
                importedCount = valid.size,
                skippedIds = parseResult.skippedIds,
                unknownIds = unknownIds,
            )
        }
    }
