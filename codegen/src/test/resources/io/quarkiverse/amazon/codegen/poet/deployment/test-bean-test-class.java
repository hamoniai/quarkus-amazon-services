package io.quarkiverse.amazon.ecr.test;

import io.quarkiverse.amazon.ecr.runtime.EcrBean;
import io.quarkus.test.QuarkusExtensionTest;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.annotations.Generated;

@Generated("io.quarkiverse.amazon:codegen")
public class EcrBeanTest {
    @RegisterExtension
    static final QuarkusExtensionTest extension = new QuarkusExtensionTest().setArchiveProducer(() -> ShrinkWrap
            .create(JavaArchive.class).addAsResource(new ClassLoaderAsset("full-config.properties"), "application.properties")
            .addClass(EcrBean.class));

    @Inject
    EcrBean bean;

    public EcrBeanTest() {
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