package com.kaua.template.infrastructure.utils;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ObservationHelper {

    private final OpenTelemetry openTelemetry;
    private final BuildProperties buildProperties;

    public ObservationHelper(final OpenTelemetry openTelemetry, final BuildProperties buildProperties) {
        this.openTelemetry = Objects.requireNonNull(openTelemetry);
        this.buildProperties = Objects.requireNonNull(buildProperties);
    }

    public <T> void observation(String spanName, Consumer<Span> block) {
        Tracer tracer = this.openTelemetry.getTracer(this.buildProperties.getName());
        Span span = tracer.spanBuilder(spanName)
                .setParent(Context.current()).startSpan();

        try (Scope scope = span.makeCurrent()) {
            block.accept(span);
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    public <T> T observationWithReturn(String spanName, Function<Span, T> block) {
        Tracer tracer = this.openTelemetry.getTracer(this.buildProperties.getName());
        Span span = tracer.spanBuilder(spanName)
                .setParent(Context.current()).startSpan();

        try (Scope scope = span.makeCurrent()) {
            return block.apply(span);
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    public <T> T observationWithValuesAndReturn(String spanName, Function<Span, T> block, String... keyValues) {
        Tracer tracer = this.openTelemetry.getTracer(this.buildProperties.getName());
        Span span = tracer.spanBuilder(spanName)
                .setParent(Context.current()).startSpan();

        // Adiciona os pares chave-valor de alta cardinalidade
        for (int i = 0; i < keyValues.length; i += 2) {
            span.setAttribute(keyValues[i], keyValues[i + 1]);
        }

        try (Scope scope = span.makeCurrent()) {
            return block.apply(span);
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
