package com.example.navigation_processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.*

class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("FeatureProcessor is running...")

        val symbols = resolver.getSymbolsWithAnnotation("com.example.navigation_annotation.FeatureLauncherApiAnnotation")
        val featureInterfaces = symbols.filterIsInstance<KSClassDeclaration>().toList()

        if (featureInterfaces.isEmpty()) {
            logger.warn("No annotated interfaces found.")
            return emptyList()
        }

        logger.warn("Generating conversionGraph with ${featureInterfaces.size} features.")

        // Подготовка файла для генерации
        val fileSpecBuilder = FileSpec.builder("com.example.generated", "FeatureGraphExtensions")

        // Создание функции conversionGraph
        val funcSpecBuilder = FunSpec.builder("conversionGraph")
            .receiver(ClassName("androidx.navigation", "NavGraphBuilder"))
            .addModifiers(KModifier.INTERNAL)
            .addParameter("startDestination", ClassName("com.example.navigation", "NavigationRoute"))
            .addParameter("navHostController", ClassName("androidx.navigation", "NavHostController"))
            .addParameter("exitFlow", LambdaTypeName.get(returnType = Unit::class.asTypeName()))

        // Добавляем в тело функции каждый найденный интерфейс как лончер
        featureInterfaces.forEach { feature ->
            val featureName = feature.simpleName.asString().replaceFirstChar { it.lowercase() }
            val className = feature.toClassName()

            // Добавляем строку для получения экземпляра лончера
            funcSpecBuilder.addStatement("val %L: %T = getKoin().get()", featureName, className)
        }

        // Добавляем вызов navigation и register для каждого лончера
        funcSpecBuilder.addCode(
            """
            navigation<ConversionGraphRoute>(
                startDestination = startDestination
            ) {
            """.trimIndent()
        )

        featureInterfaces.forEach { feature ->
            val featureName = feature.simpleName.asString().replaceFirstChar { it.lowercase() }
            funcSpecBuilder.addCode(
                """
                register(
                    featureLauncherApi = $featureName,
                    navController = navHostController,
                    exitFlow = exitFlow
                )
                """.trimIndent()
            )
        }

        funcSpecBuilder.addCode("}")

        // Добавляем сгенерированную функцию в файл
        fileSpecBuilder.addFunction(funcSpecBuilder.build())

        // Записываем файл
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "com.example.generated",
            fileName = "FeatureGraphExtensions"
        ).use { stream ->
            stream.writer().use { writer ->
                fileSpecBuilder.build().writeTo(writer)
            }
        }

        return emptyList()
    }
}


// Функция для получения ClassName из KSClassDeclaration
fun KSClassDeclaration.toClassName(): ClassName {
    return ClassName(this.packageName.asString(), this.simpleName.asString())
}
