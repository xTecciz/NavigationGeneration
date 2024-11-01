package com.example.navigation_processor

import com.example.navigation_annotation.GenerateNavigationGraph
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File

class GraphProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("[GraphProcessor] Processor is running...")

        // Поиск интерфейса с аннотацией @GenerateNavigationGraph
        val targetInterfaceSymbols = resolver.getSymbolsWithAnnotation(GenerateNavigationGraph::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .toList()

        if (targetInterfaceSymbols.isEmpty()) {
            logger.warn("[GraphProcessor] No @GenerateNavigationGraph annotated interface found. Skipping generation.")
            return emptyList()
        }

        targetInterfaceSymbols.forEach { symbol ->
            logger.warn("[GraphProcessor] Found @GenerateNavigationGraph annotation on interface: ${symbol.simpleName.asString()}")
        }

        val targetInterface = targetInterfaceSymbols.first()
        val targetInterfaceName = targetInterface.simpleName.asString()

        // Получение пути для временных файлов KSP
        val outputDir = environment.options["kspGeneratedDir"] ?: "build/generated/ksp/debug/kotlin"

        logger.warn("[GraphProcessor] Looking for feature files in: $outputDir")

        // Проверка существования каталога
        val featureFilesDir = File(outputDir)
        if (!featureFilesDir.exists()) {
            logger.warn("[GraphProcessor] Output directory not found: $outputDir")
            return emptyList()
        }

        // Поиск файлов с именами, начинающимися на "FeaturesIn_"
        val featureFiles = featureFilesDir.walkTopDown()
            .filter { it.isFile && it.extension == "txt" && it.name.startsWith("FeaturesIn_") }
            .toList()

        if (featureFiles.isEmpty()) {
            logger.warn("[GraphProcessor] No feature list files found in $outputDir.")
            return emptyList()
        } else {
            featureFiles.forEach { file ->
                logger.warn("[GraphProcessor] Found feature file: ${file.name}")
            }
        }

        // Чтение имен классов из всех найденных файлов
        val featureClasses = featureFiles.flatMap { file ->
            file.readLines()
        }

        if (featureClasses.isEmpty()) {
            logger.warn("[GraphProcessor] No feature interfaces found in the collected files.")
            return emptyList()
        }

        logger.warn("[GraphProcessor] Generating FeatureGraphImpl class with ${featureClasses.size} feature(s).")

        // Создаем файл и класс, который будет наследоваться от targetInterface
        val fileSpecBuilder = FileSpec.builder("com.example.generated", "FeatureGraphImpl")

        val classBuilder = TypeSpec.classBuilder("FeatureGraphImpl")
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(ClassName(targetInterface.packageName.asString(), targetInterfaceName))

        // Добавляем функции инициализации для каждого найденного интерфейса
        featureClasses.forEach { className ->
            val shortName = className.substringAfterLast(".").replaceFirstChar { it.lowercase() }
            val featureClass = ClassName.bestGuess(className)

            classBuilder.addProperty(
                PropertySpec.builder(shortName, featureClass)
                    .initializer("getKoin().get()")
                    .build()
            )

            classBuilder.addFunction(
                FunSpec.builder("register${featureClass.simpleName}")
                    .addParameter("navController", ClassName("androidx.navigation", "NavHostController"))
                    .addParameter("exitFlow", LambdaTypeName.get(returnType = Unit::class.asTypeName()))
                    .addStatement("register(featureLauncherApi = $shortName, navController = navController, exitFlow = exitFlow)")
                    .build()
            )
        }

        // Добавляем сгенерированный класс в файл
        fileSpecBuilder.addType(classBuilder.build())

        try {
            codeGenerator.createNewFile(
                Dependencies(aggregating = true),
                "com.example.generated",
                "FeatureGraphImpl"
            ).use { stream ->
                stream.writer().use { writer ->
                    fileSpecBuilder.build().writeTo(writer)
                }
            }
            logger.warn("[GraphProcessor] Generated FeatureGraphImpl successfully.")
        } catch (e: Exception) {
            logger.warn("[GraphProcessor] Error generating FeatureGraphImpl: ${e.message}")
        }

        return emptyList()
    }
}
