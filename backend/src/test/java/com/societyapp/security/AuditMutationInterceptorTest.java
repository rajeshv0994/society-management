package com.societyapp.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.port.in.AuditUseCase;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

class AuditMutationInterceptorTest {
    private final AuditUseCase audits = mock(AuditUseCase.class);
    private final AuditMutationInterceptor interceptor = new AuditMutationInterceptor(audits);

    @Test
    void capturesSuccessfulMutationsWithoutRequestBody() throws Exception {
        var request = new MockHttpServletRequest("PATCH", "/api/widgets/" + UUID.randomUUID());
        var response = new MockHttpServletResponse();
        var id = UUID.randomUUID().toString();
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/widgets/{id}");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", id));
        var handler =
                new HandlerMethod(
                        new SampleController(),
                        SampleController.class.getDeclaredMethod("updateWidget", UUID.class));

        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, null);

        verify(audits)
                .record(
                        argThat(
                                command ->
                                        command.societyId() == null
                                                && command.actorId() == null
                                                && command.action().equals("API_UPDATE_WIDGET")
                                                && command.entityType().equals("SAMPLE")
                                                && command.entityId().equals(id)
                                                && command.sensitive()
                                                && command.details()
                                                        .values()
                                                        .equals(
                                                                Map.of(
                                                                        "http_method",
                                                                        "PATCH",
                                                                        "route",
                                                                        "/api/widgets/{id}",
                                                                        "outcome",
                                                                        "SUCCEEDED"))));
    }

    @Test
    void ignoresReadsAndFailedMutations() throws Exception {
        var read = new MockHttpServletRequest("GET", "/api/widgets");
        var response = new MockHttpServletResponse();
        var readHandler =
                new HandlerMethod(
                        new SampleController(),
                        SampleController.class.getDeclaredMethod("widgets"));
        interceptor.preHandle(read, response, readHandler);
        interceptor.afterCompletion(read, response, readHandler, null);

        var failed = new MockHttpServletRequest("PATCH", "/api/widgets/bad");
        var failedResponse = new MockHttpServletResponse();
        failedResponse.setStatus(400);
        var mutationHandler =
                new HandlerMethod(
                        new SampleController(),
                        SampleController.class.getDeclaredMethod("updateWidget", UUID.class));
        interceptor.preHandle(failed, failedResponse, mutationHandler);
        interceptor.afterCompletion(failed, failedResponse, mutationHandler, null);

        verifyNoInteractions(audits);
    }

    @RestController
    static class SampleController {
        @GetMapping("/api/widgets")
        void widgets() {}

        @PatchMapping("/api/widgets/{id}")
        void updateWidget(@PathVariable UUID id) {}
    }
}
