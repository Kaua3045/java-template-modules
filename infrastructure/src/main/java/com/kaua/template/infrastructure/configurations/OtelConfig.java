package com.kaua.template.infrastructure.configurations;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
public class OtelConfig {

    @Profile("test-integration")
    @Bean
    public OpenTelemetrySdk openTelemetrySdk() {
        final var aInMemorySpanExporter = InMemorySpanExporter.create();

        final var aSdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(aInMemorySpanExporter))
                .addResource(Resource.getDefault()
                        .merge(Resource.create(Attributes.builder().put("service.name", "template").build())))
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(aSdkTracerProvider)
                .setPropagators(ContextPropagators.create(B3Propagator.injectingSingleHeader()))
                .build();
    }
}
