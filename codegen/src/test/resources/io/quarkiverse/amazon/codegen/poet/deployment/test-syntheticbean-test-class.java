package io.quarkiverse.amazon.ecr.test;

import io.quarkiverse.amazon.ecr.deployment.EcrTestProcessor;
import io.quarkiverse.amazon.ecr.runtime.EcrSyntheticBean;
import io.quarkus.test.QuarkusExtensionTest;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.annotations.Generated;

@Generated("io.quarkiverse.amazon:codegen")
public class EcrSyntheticBeanTest {
    @RegisterExtension
    static final QuarkusExtensionTest extension = new QuarkusExtensionTest().setArchiveProducer(() -> ShrinkWrap
            .create(JavaArchive.class).addClasses(EcrTestProcessor.class)
            .addAsResource(new ClassLoaderAsset("full-config.properties"), "application.properties")
            .addAsResource(new StringAsset(EcrTestProcessor.class.getName()), "META-INF/quarkus-build-steps.list"));

    @Inject
    EcrSyntheticBean bean;

    public EcrSyntheticBeanTest() {
    }

    @Test
    public void fullConfig() {
        Assertions.assertNotNull(bean);
        Assertions.assertDoesNotThrow(() -> {
            bean.invokeAsyncClient();
        });
        Assertions.assertDoesNotThrow(() -> {
            bean.invokeSyncClient();
        });
    }
}