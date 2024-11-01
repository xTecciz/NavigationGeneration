package com.example.navigation_processor

import com.example.navigation_annotation.FeatureLauncherApiAnnotation
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.IOException


class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("FeatureProcessor is running...")

        // Получаем все символы, аннотированные @FeatureLauncherApiAnnotation
        val symbols = resolver.getSymbolsWithAnnotation("com.example.navigation_annotation.FeatureLauncherApiAnnotation")
        symbols.forEach { symbol ->
            if (symbol is KSClassDeclaration) {
                // Логируем, если символ является классом или интерфейсом
                logger.warn("Found class or interface: ${symbol.simpleName.asString()} of type: ${symbol::class.simpleName}")
            } else {
                // Логируем символы других типов для отладки
                logger.warn("Found non-class symbol: ${symbol::class.simpleName}")
            }
        }

        val featureInterfaces = symbols.filterIsInstance<KSClassDeclaration>().toList()
        if (featureInterfaces.isEmpty()) {
            logger.warn("No annotated interfaces found.")
            return emptyList()
        }

        logger.warn("Generating FeatureGraph with ${featureInterfaces.size} features.")

        // Подготовка к генерации класса графа
        val fileSpecBuilder = FileSpec.builder("com.example.generated", "FeatureGraph")
        val classBuilder = TypeSpec.objectBuilder("FeatureGraph")

        // Создание свойств для каждого аннотированного интерфейса
        featureInterfaces.forEach { feature ->
            val featureName = feature.simpleName.asString().replaceFirstChar { it.lowercase() }
            val propertySpec = PropertySpec.builder(featureName, feature.toClassName())
                .initializer("%T()", feature.toClassName())
                .build()
            classBuilder.addProperty(propertySpec)
        }

        // Добавляем сгенерированный класс в файл
        fileSpecBuilder.addType(classBuilder.build())

        // Запись сгенерированного кода в файл
        try {
            codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = true),
                packageName = "com.example.generated",
                fileName = "FeatureGraph"
            ).use { stream ->
                stream.writer().use { writer ->
                    fileSpecBuilder.build().writeTo(writer)
                }
            }
        } catch (e: IOException) {
            logger.warn("File already exists or could not be created: ${e.message}")
        }

        return emptyList()
    }
}

// Функция для получения ClassName из KSClassDeclaration
fun KSClassDeclaration.toClassName(): ClassName {
    return ClassName(this.packageName.asString(), this.simpleName.asString())
}
