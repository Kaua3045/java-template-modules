package com.kaua.template.infrastructure.utils;

import com.kaua.template.domain.UnitTest;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.info.BuildProperties;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ObservationHelperTest extends UnitTest {

    @Mock
    private OpenTelemetry openTelemetry;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private Scope scope;

    @Mock
    private SpanBuilder spanBuilder;

    @Mock
    private BuildProperties buildProperties;

    private ObservationHelper observationHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(buildProperties.getName()).thenReturn("test-app");
        when(openTelemetry.getTracer("test-app")).thenReturn(tracer);
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setParent(any())).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
    }

    @Test
    void testObservationExecutesBlockAndEndsSpan() {
        doNothing().when(span).end();
        Consumer<Span> block = mock(Consumer.class);

        observationHelper = new ObservationHelper(openTelemetry, buildProperties);
        observationHelper.observation("test-span", block);

        verify(block).accept(span);
        verify(span).end();
    }

    @Test
    void testObservationRecordsExceptionAndThrows() {
        doNothing().when(span).end();
        Consumer<Span> block = mock(Consumer.class);
        doThrow(new RuntimeException("Test Exception")).when(block).accept(span);

        observationHelper = new ObservationHelper(openTelemetry, buildProperties);
        Exception exception = assertThrows(RuntimeException.class, () ->
                observationHelper.observation("test-span", block)
        );

        assertEquals("Test Exception", exception.getMessage());
        verify(span).recordException(exception);
        verify(span).end();
    }

    @Test
    void testObservationWithReturnExecutesBlockAndReturnsValue() {
        Function<Span, String> block = mock(Function.class);
        when(block.apply(span)).thenReturn("result");

        observationHelper = new ObservationHelper(openTelemetry, buildProperties);
        String result = observationHelper.observationWithReturn("test-span", block);

        assertEquals("result", result);
        verify(block).apply(span);
        verify(span).end();
    }

    @Test
    void testObservationWithReturnRecordsExceptionAndThrows() {
        Function<Span, String> block = mock(Function.class);
        doThrow(new RuntimeException("Test Exception")).when(block).apply(span);

        observationHelper = new ObservationHelper(openTelemetry, buildProperties);
        Exception exception = assertThrows(RuntimeException.class, () ->
                observationHelper.observationWithReturn("test-span", block)
        );

        assertEquals("Test Exception", exception.getMessage());
        verify(span).recordException(exception);
        verify(span).end();
    }

    @Test
    void testObservationWithValuesAndReturnAddsAttributesAndReturnsValue() {
        Function<Span, String> block = mock(Function.class);
        when(block.apply(span)).thenReturn("result");

        observationHelper = new ObservationHelper(openTelemetry, buildProperties);
        String result = observationHelper.observationWithValuesAndReturn("test-span", block, "key1", "value1", "key2", "value2");

        assertEquals("result", result);
        verify(span).setAttribute("key1", "value1");
        verify(span).setAttribute("key2", "value2");
        verify(block).apply(span);
        verify(span).end();
    }

    @Test
    void testObservationWithReturnAndAttributesRecordsExceptionAndThrows() {
        doNothing().when(span).end();
        Function<Span, ?> block = mock(Function.class);
        doThrow(new RuntimeException("Test Exception")).when(block).apply(span);

        observationHelper = new ObservationHelper(openTelemetry, buildProperties);
        Exception exception = assertThrows(RuntimeException.class, () ->
                observationHelper.observationWithValuesAndReturn("test-span", block, "teste", "value")
        );

        assertEquals("Test Exception", exception.getMessage());
        verify(span).recordException(exception);
        verify(span).end();
    }
}
