package com.stavro_xhardha.producer_processor

import com.google.auto.service.AutoService
import com.google.common.collect.Iterables.getOnlyElement
import com.squareup.kotlinpoet.*
import com.stavro_xhardha.producer.Producer
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.extractFullName
import me.eugeniomarletti.kotlin.metadata.isPrimary
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter.methodsIn
import javax.tools.Diagnostic

@AutoService(Processor::class)
class ProducerProcessor : KotlinAbstractProcessor() {

    private val viewModelProducerAnnotation = Producer::class.java


    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(viewModelProducerAnnotation)
            .asSequence()
            .map { it as TypeElement }
            .forEach { annotatedElement ->
                if (annotatedElement.kind == ElementKind.CLASS
                    && annotatedElement.superclass.asTypeName().toString().equals(
                        "androidx.lifecycle.ViewModel",
                        false
                    )
                ) {
                    val pack = elementUtils.getPackageOf(annotatedElement).toString()
                    val annotatedCLassName = annotatedElement.simpleName.toString()
                    val kaptKotlinGeneratedDir = options[KOTLIN_DIRECTORY_NAME]
                    val metadata = annotatedElement.kotlinMetadata as KotlinClassMetadata

                    createKotlinFile(
                        pack,
                        annotatedCLassName,
                        kaptKotlinGeneratedDir,
                        metadata
                    )

                } else {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "${annotatedElement.simpleName} must be a class extending androidx.lifecycle.ViewModel"
                    )
                }

            }
        return false
    }

    private fun createKotlinFile(
        pack: String,
        annotatedCLassName: String,
        kaptKotlinGeneratedDir: String?,
        metadata: KotlinClassMetadata
    ) {
        val mainConstructor = metadata.data.classProto.constructorList.find { it.isPrimary }
        val nameResolver = metadata.data.nameResolver

        if (mainConstructor?.valueParameterList?.size == 0) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "You don't need a ViewModelFactory with 0 constructor Arguments"
            )
        } else {

            val primaryConstructor = FunSpec.constructorBuilder().apply {

                mainConstructor?.valueParameterList?.forEach {
                    addParameter(
                        name = nameResolver.getString(it.name),
                        type = ClassName.bestGuess(
                            it.type.extractFullName(metadata.data).replace("`", "")
                        )
                    ).build()
                }
            }

            val typeElement =
                elementUtils.getTypeElement("androidx.lifecycle.ViewModelProvider.Factory")

            val overridingMethod =
                FunSpec.overriding(getOnlyElement(methodsIn(typeElement.enclosedElements))).addCode(
                    "return $annotatedCLassName${generateViewModelConstructor(
                        mainConstructor?.valueParameterList,
                        nameResolver
                    )} as T"
                )

            val generatingClass = TypeSpec.classBuilder("${annotatedCLassName}Factory")
                .primaryConstructor(primaryConstructor.build()).apply {
                    mainConstructor?.valueParameterList?.forEach {
                        addProperty(
                            PropertySpec.builder(
                                name = nameResolver.getString(it.name),
                                type = ClassName.bestGuess(
                                    it.type.extractFullName(metadata.data).replace("`", "")
                                )
                            ).initializer(metadata.data.nameResolver.getString(it.name)).build()
                        ).build()
                    }
                }.addSuperinterface(
                    ClassName(
                        "androidx.lifecycle.ViewModelProvider",
                        "Factory"
                    )
                ).addFunction(overridingMethod.build())

            val file = FileSpec.builder(pack, "${annotatedCLassName}Factory").apply {
                addType(generatingClass.build())
            }.build()

            file.writeTo(File(kaptKotlinGeneratedDir, "$${annotatedCLassName}Factory.kt"))
        }
    }

    private fun generateViewModelConstructor(
        valueParameterList: List<ProtoBuf.ValueParameter>?,
        nameResolver: NameResolver
    ): String {
        var stringToGenerate = "("
        valueParameterList?.forEach {
            stringToGenerate += nameResolver.getString(it.name) + ","
        }

        stringToGenerate = stringToGenerate.substring(0, stringToGenerate.length - 1)

        stringToGenerate += ")"

        return stringToGenerate
    }

    override fun getSupportedAnnotationTypes(): Set<String> =
        setOf(viewModelProducerAnnotation.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    companion object {
        const val KOTLIN_DIRECTORY_NAME = "kapt.kotlin.generated"
    }
}