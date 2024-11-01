package com.example.navigation_processor

import com.example.navigation_annotation.FeatureLauncherApiAnnotation
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

class CombinedProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("[CombinedProcessor] Processor is running...")

        // Этап 1: Находим все интерфейсы с аннотацией @FeatureLauncherApiAnnotation
        val featureSymbols = resolver.getSymbolsWithAnnotation(FeatureLauncherApiAnnotation::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (featureSymbols.isEmpty()) {
            logger.warn("[CombinedProcessor] No annotated feature interfaces found.")
            return emptyList()
        }

        // Создаем временные файлы для каждого найденного интерфейса
        val moduleFileName = "FeaturesIn_${featureSymbols.first().simpleName.asString()}"
        logger.warn("[CombinedProcessor] Preparing to generate $moduleFileName.txt with ${featureSymbols.size} features.")

        val outputDir = environment.options["kspGeneratedDir"] ?: "build/generated/ksp/debug/resources"
        val featureFile = File(outputDir, "$moduleFileName.txt")
        featureFile.parentFile.mkdirs()
        featureFile.writeText(featureSymbols.joinToString("\n") { it.qualifiedName!!.asString() })

        logger.warn("[CombinedProcessor] Generated file $moduleFileName.txt with ${featureSymbols.size} feature(s).")

        // Этап 2: Поиск интерфейса с аннотацией @GenerateNavigationGraph для создания хоста
        val targetInterfaceSymbols = resolver.getSymbolsWithAnnotation(GenerateNavigationGraph::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .toList()

        if (targetInterfaceSymbols.isEmpty()) {
            logger.warn("[CombinedProcessor] No @GenerateNavigationGraph annotated interface found. Skipping generation.")
            return emptyList()
        }

        val targetInterface = targetInterfaceSymbols.first()
        val targetInterfaceName = targetInterface.simpleName.asString()

        // Чтение всех временных файлов с именами, начинающимися на "FeaturesIn_"
        val featureFilesDir = File(outputDir)
        val featureClasses = featureFilesDir.walkTopDown()
            .filter { it.isFile && it.extension == "txt" && it.name.startsWith("FeaturesIn_") }
            .flatMap { it.readLines() }
            .toList()

        if (featureClasses.isEmpty()) {
            logger.warn("[CombinedProcessor] No feature interfaces found in the collected files.")
            return emptyList()
        }

        logger.warn("[CombinedProcessor] Generating FeatureGraphImpl class with ${featureClasses.size} feature(s).")

        // Создаем файл и класс FeatureGraphImpl, который будет наследоваться от targetInterface
        val fileSpecBuilder = FileSpec.builder("com.example.generated", "FeatureGraphImpl")

        val classBuilder = TypeSpec.classBuilder("FeatureGraphImpl")
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(ClassName(targetInterface.packageName.asString(), targetInterfaceName))

        // Добавляем свойства и функции регистрации для каждого интерфейса
        featureClasses.forEach { className ->
            val shortName = className.substringAfterLast(".").replaceFirstChar { it.lowercase() }
            val featureClass = ClassName.bestGuess(className)

            // Добавляем свойство для каждого интерфейса
            classBuilder.addProperty(
                PropertySpec.builder(shortName, featureClass)
                    .initializer("getKoin().get()")
                    .build()
            )

            // Добавляем метод регистрации для каждого интерфейса
            classBuilder.addFunction(
                FunSpec.builder("register${featureClass.simpleName}")
                    .addParameter("navController", ClassName("androidx.navigation", "NavHostController"))
                    .addParameter("exitFlow", LambdaTypeName.get(returnType = Unit::class.asTypeName()))
                    .addStatement(
                        "register(featureLauncherApi = $shortName, navController = navController, exitFlow = exitFlow)"
                    )
                    .build()
            )
        }

        // Добавляем сгенерированный класс в файл
        fileSpecBuilder.addType(classBuilder.build())

        // Записываем класс FeatureGraphImpl
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
            logger.warn("[CombinedProcessor] Generated FeatureGraphImpl successfully.")
        } catch (e: Exception) {
            logger.warn("[CombinedProcessor] Error generating FeatureGraphImpl: ${e.message}")
        }

        return emptyList()
    }
}

