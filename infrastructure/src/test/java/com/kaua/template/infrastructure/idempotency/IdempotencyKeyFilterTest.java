package com.kaua.template.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.template.IntegrationTest;
import com.kaua.template.domain.utils.IdentifierUtils;
import com.kaua.template.infrastructure.idempotency.gateways.IdempotencyKeyGateway;
import com.kaua.template.infrastructure.utils.ObservationHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@IntegrationTest
@AutoConfigureMockMvc
public class IdempotencyKeyFilterTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IdempotencyKeyGateway idempotencyKeyGateway;

    @Autowired
    private ObservationHelper observationHelper;

    @Test
    void givenAValidPostMethodWithValidNonExistsIdempotencyKey_whenCallEndpoint_thenReturnSuccess() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var aBody = new IdempotencyKeyBodyTest(aId);

        final var request = MockMvcRequestBuilders.post("/test/idempotency-key-helper/success")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-idempotency-key", IdentifierUtils.generateNewIdWithoutHyphen())
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/test/idempotency-key-helper/" + aId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));
    }

    @Test
    void givenAValidPostMethodWithValidExistsIdempotencyKey_whenCallEndpoint_thenReturnSuccess() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();
        final var aKey = IdentifierUtils.generateNewIdWithoutHyphen();

        final var aBody = new IdempotencyKeyBodyTest(aId);

        final var aFirstRequest = MockMvcRequestBuilders.post("/test/idempotency-key-helper/success")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-idempotency-key", aKey)
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(aFirstRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/test/idempotency-key-helper/" + aId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));

        final var request = MockMvcRequestBuilders.post("/test/idempotency-key-helper/success")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-idempotency-key", aKey)
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/test/idempotency-key-helper/" + aId))
                .andExpect(MockMvcResultMatchers.header().string("x-idempotency-response", "true"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));
    }

    @Test
    void givenAValidGetMethodWithoutIdempotencyKeyAnnotation_whenCallEndpoint_thenReturnSuccess() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var request = MockMvcRequestBuilders.get("/test/idempotency-key-helper/" + aId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));
    }

    @Test
    void givenAValidPatchMethodWithValidNonExistsIdempotencyKey_whenCallEndpoint_thenReturnSuccess() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var aBody = new IdempotencyKeyBodyTest(aId);

        final var request = MockMvcRequestBuilders.patch("/test/idempotency-key-helper/patch/" + aId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-idempotency-key", IdentifierUtils.generateNewIdWithoutHyphen())
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));
    }

    @Test
    void givenAValidPutMethodWithValidNonExistsIdempotencyKey_whenCallEndpoint_thenReturnSuccess() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var aBody = new IdempotencyKeyBodyTest(aId);

        final var request = MockMvcRequestBuilders.put("/test/idempotency-key-helper/put/idempotency/" + aId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-idempotency-key", IdentifierUtils.generateNewIdWithoutHyphen())
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));
    }

    @Test
    void givenASupportedPutMethodButWithoutIdempotencyKeyAnnotation_whenCallEndpoint_thenReturnSuccess() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var aBody = new IdempotencyKeyBodyTest(aId);

        final var request = MockMvcRequestBuilders.put("/test/idempotency-key-helper/put/" + aId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(aId));
    }

    @Test
    void givenAValidPutMethodButWithoutIdempotencyHeader_whenCallEndpoint_thenReturnError() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var aBody = new IdempotencyKeyBodyTest(aId);

        final var request = MockMvcRequestBuilders.put("/test/idempotency-key-helper/put/idempotency/" + aId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(aBody));

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Idempotency key required and the required header is 'x-idempotency-key'"));
    }

    @Test
    void givenAnInvalidGetMethodWithIdempotencyKeyAnnotation_whenCallEndpoint_thenReturnError() throws Exception {
        final var aId = IdentifierUtils.generateNewIdWithoutHyphen();

        final var request = MockMvcRequestBuilders.get("/test/idempotency-key-helper/get/" + aId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Idempotency key is not supported for this method: GET"));
    }

    @Test
    void testOnHandlerMethodThrows() throws Exception {
        final var aRequest = Mockito.mock(HttpServletRequest.class);
        final var aResponse = Mockito.mock(HttpServletResponse.class);
        final var aFilterChain = Mockito.mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);
        final var aRequestMappingHandlerMapping = Mockito.mock(RequestMappingHandlerMapping.class);

        final var aIdempotencyKeyFilter = new IdempotencyKeyFilter(
                idempotencyKeyGateway,
                aRequestMappingHandlerMapping,
                aHandlerExceptionResolver,
                observationHelper
        );

        Mockito.when(aRequestMappingHandlerMapping.getHandler(aRequest))
                .thenThrow(new RuntimeException("test"));

        aIdempotencyKeyFilter.doFilterInternal(aRequest, aResponse, aFilterChain);

        Mockito.verify(aHandlerExceptionResolver, Mockito.times(1))
                .resolveException(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testOnHandlerMethodReturnsNull() throws Exception {
        final var aRequest = Mockito.mock(HttpServletRequest.class);
        final var aResponse = Mockito.mock(HttpServletResponse.class);
        final var aFilterChain = Mockito.mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);
        final var aRequestMappingHandlerMapping = Mockito.mock(RequestMappingHandlerMapping.class);

        final var aIdempotencyKeyFilter = new IdempotencyKeyFilter(
                idempotencyKeyGateway,
                aRequestMappingHandlerMapping,
                aHandlerExceptionResolver,
                observationHelper
        );

        Mockito.when(aRequestMappingHandlerMapping.getHandler(aRequest))
                .thenReturn(null);

        Assertions.assertDoesNotThrow(() -> aIdempotencyKeyFilter.doFilterInternal(aRequest, aResponse, aFilterChain));
    }

    @Test
    void testOnHandlerMethodIsNotHandlerMethod() throws Exception {
        final var aRequest = Mockito.mock(HttpServletRequest.class);
        final var aResponse = Mockito.mock(HttpServletResponse.class);
        final var aFilterChain = Mockito.mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);
        final var aRequestMappingHandlerMapping = Mockito.mock(RequestMappingHandlerMapping.class);

        final var aIdempotencyKeyFilter = new IdempotencyKeyFilter(
                idempotencyKeyGateway,
                aRequestMappingHandlerMapping,
                aHandlerExceptionResolver,
                observationHelper
        );

        final var aHandlerExecutionChain = Mockito.mock(HandlerExecutionChain.class);

        Mockito.when(aRequestMappingHandlerMapping.getHandler(aRequest))
                .thenReturn(aHandlerExecutionChain);

        Assertions.assertDoesNotThrow(() -> aIdempotencyKeyFilter.doFilterInternal(aRequest, aResponse, aFilterChain));
    }
}
