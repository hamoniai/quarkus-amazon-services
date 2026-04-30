package io.quarkiverse.amazon.codegen.poet.deployment;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.quarkiverse.amazon.codegen.poet.PoetUtils;
import software.amazon.awssdk.codegen.model.intermediate.IntermediateModel;
import software.amazon.awssdk.codegen.poet.ClassSpec;

public class TestBeanTestClass implements ClassSpec {
    private final IntermediateModel model;
    private final ClassName beanClassName;
    private final ClassName testClassName;

    private final String basePackage;
    private final String quarkusBasePackage;
    private final String quarkusRuntimePackage;
    private final String quarkusTestPackage;

    private final ClassName testAnnotation = ClassName.get("org.junit.jupiter.api", "Test");
    private final ClassName injectAnnotation = ClassName.get("jakarta.inject", "Inject");
    private final ClassName registerExtensionAnnotation = ClassName.get("org.junit.jupiter.api.extension", "RegisterExtension");

    private final ClassName assertionsType = ClassName.get("org.junit.jupiter.api", "Assertions");
    private final ClassName classLoaderAssetType = ClassName.get("org.jboss.shrinkwrap.api.asset", "ClassLoaderAsset");
    private final ClassName extensionTestType = ClassName.get("io.quarkus.test", "QuarkusExtensionTest");
    private final ClassName javaArchiveType = ClassName.get("org.jboss.shrinkwrap.api.spec", "JavaArchive");
    private final ClassName shrinkWrapType = ClassName.get("org.jboss.shrinkwrap.api", "ShrinkWrap");

    private final String beanName = "bean";

    public TestBeanTestClass(IntermediateModel model) {
        this.model = model;
        this.basePackage = model.getMetadata().getFullClientPackageName();
        this.quarkusBasePackage = "io.quarkiverse.amazon." + model.getMetadata().getClientPackageName();
        this.quarkusRuntimePackage = quarkusBasePackage + ".runtime";
        this.quarkusTestPackage = quarkusBasePackage + ".test";

        this.beanClassName = ClassName.get(this.quarkusRuntimePackage,
                model.getMetadata().getServiceName() + "Bean");
        this.testClassName = ClassName.get(this.quarkusTestPackage,
                model.getMetadata().getServiceName() + "BeanTest");
    }

    @Override
    public TypeSpec poetSpec() {
        TypeSpec.Builder builder = PoetUtils.createClassBuilder(testClassName)
                .addModifiers(PUBLIC)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(PUBLIC).build());

        // The archive lambda
        var archiveProducerLambda = CodeBlock.builder()
                .add("() -> $T.create($T.class)", shrinkWrapType, javaArchiveType)
                .add(".addAsResource(new $T($S), $S)", classLoaderAssetType, "full-config.properties", "application.properties")
                .add(".addClass($T.class)", beanClassName)
                .build();

        // Add beans
        builder.addField(
                FieldSpec.builder(beanClassName, beanName)
                        .addAnnotation(injectAnnotation)
                        .build())
                .addField(
                        // Add static test initialiser
                        FieldSpec.builder(extensionTestType, "extension", STATIC, FINAL)
                                .addAnnotation(registerExtensionAnnotation)
                                .initializer(
                                        CodeBlock.builder()
                                                .add("new $T()", extensionTestType)
                                                .add(".setArchiveProducer($L)", archiveProducerLambda)
                                                .build())
                                .build());

        // Add test methods
        builder.addMethod(addFullConfigTest());
        return builder.build();
    }

    /**
     * A simple test that uses runtime config to verify that a client has been created.
     */
    private MethodSpec addFullConfigTest() {
        return MethodSpec.methodBuilder("fullConfig")
                .addModifiers(PUBLIC)
                .addAnnotation(testAnnotation)
                .addStatement("$T.assertNotNull($N)", assertionsType, beanName)
                .addStatement("$T.assertDoesNotThrow(() -> { $N.invokeAsyncClient(); })", assertionsType, beanName)
                .addStatement("$T.assertDoesNotThrow(() -> { $N.invokeSyncClient(); })", assertionsType, beanName)
                .build();
    }

    @Override
    public ClassName className() {
        return testClassName;
    }
}
