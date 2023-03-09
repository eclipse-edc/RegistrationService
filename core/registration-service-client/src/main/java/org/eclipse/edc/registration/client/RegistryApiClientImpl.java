package org.eclipse.edc.registration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.edc.registration.client.model.ParticipantDto;
import org.eclipse.edc.registration.client.response.ApiResult;
import org.eclipse.edc.spi.http.EdcHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Function;

class RegistryApiClientImpl implements RegistryApiClient {
    private final EdcHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String baseUrl;

    RegistryApiClientImpl(EdcHttpClient httpClient, String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApiResult<Void> addParticipant() {
        var url = URI.create(baseUrl + "/registry/participant");
        var request = new Request.Builder()
                .post(RequestBody.create(new byte[0]))
                .url(url.toString())
                .build();

        return execute(request).map(s -> null);
    }

    @Override
    public ApiResult<List<ParticipantDto>> listParticipants() {

        var url = URI.create(baseUrl + "/registry/participants");
        var request = new Request.Builder()
                .url(url.toString())
                .get()
                .build();
        var result = execute(request);

        return result.map(as(new TypeReference<>() {
        }));
    }

    @Override
    public ApiResult<ParticipantDto> getParticipant() {
        var url = URI.create(baseUrl + "/registry/participant");
        var request = new Request.Builder()
                .url(url.toString())
                .get()
                .build();
        return execute(request).map(as(new TypeReference<>() {
        }));
    }

    @Override
    public void updateBaseUri(String uri) {
        baseUrl = uri;
    }

    // the TypeReference must be passed here explicitly, otherwise Jackson can't resolve the erased type anymore
    private <R> Function<String, R> as(TypeReference<R> listTypeReference) {
        return json -> {
            try {
                return objectMapper.readValue(json, listTypeReference);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private ApiResult<String> execute(Request rq) {
        try {
            var response = httpClient.execute(rq);
            return response.isSuccessful() ?
                    ApiResult.success(getMessage(response)) :
                    ApiResult.failure(response.code(), response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMessage(Response response) throws IOException {
        var body = response.body();
        if (body != null) {
            var str = body.string();
            return str.isBlank() ? null : str;
        }
        return null;
    }

}
