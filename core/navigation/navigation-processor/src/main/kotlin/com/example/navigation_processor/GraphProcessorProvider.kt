package com.example.navigation_processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class GraphProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("[GraphProcessorProvider] Initializing GraphProcessor...")
        return GraphProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            environment = environment // Передаем environment в GraphProcessor
        )
    }
}
