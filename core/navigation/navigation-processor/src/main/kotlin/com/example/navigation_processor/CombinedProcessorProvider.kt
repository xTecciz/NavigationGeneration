package com.example.navigation_processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class CombinedProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("[CombinedProcessorProvider] Initializing CombinedProcessor...")
        return CombinedProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            environment = environment
        )
    }
}