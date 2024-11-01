package com.example.navigation_processor

import com.example.navigation_annotation.FeatureLauncherApiAnnotation
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import java.io.OutputStreamWriter

class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("[FeatureProcessor] FeatureProcessor is running...")

        val featureSymbols = resolver.getSymbolsWithAnnotation(FeatureLauncherApiAnnotation::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (featureSymbols.isEmpty()) {
            logger.warn("No annotated feature interfaces found.")
            return emptyList()
        }

        // Создаем уникальное имя файла на основе имени первого аннотированного класса, без расширения
        val moduleFileName = "FeaturesIn_${featureSymbols.first().simpleName.asString()}"
        logger.warn("[FeatureProcessor] Preparing to generate $moduleFileName.txt with ${featureSymbols.size} features.")

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "",
            fileName = moduleFileName,
            extensionName = "txt"
        ).use { stream ->
            OutputStreamWriter(stream).use { writer ->
                featureSymbols.forEach { feature ->
                    writer.write("${feature.qualifiedName?.asString()}\n")
                    logger.warn("[FeatureProcessor] Writing ${feature.qualifiedName?.asString()} to $moduleFileName.txt")
                }
            }
        }

        logger.warn("[FeatureProcessor] Generated file $moduleFileName.txt with ${featureSymbols.size} feature(s).")
        return emptyList()
    }
}

// Функция для получения ClassName из KSClassDeclaration
fun KSClassDeclaration.toClassName(): ClassName {
    return ClassName(this.packageName.asString(), this.simpleName.asString())
}
