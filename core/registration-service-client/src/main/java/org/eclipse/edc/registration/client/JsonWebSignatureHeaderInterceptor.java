/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.registration.client;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

public class JsonWebSignatureHeaderInterceptor implements Consumer<HttpRequest.Builder>, Interceptor {

    private final Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider;
    private final String audience;

    public JsonWebSignatureHeaderInterceptor(Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider, String audience) {
        this.credentialsProvider = credentialsProvider;
        this.audience = audience;
    }

    @Override
    public void accept(HttpRequest.Builder b) {
        var credentialResult = createCredential();
        b.header("Authorization", format("Bearer %s", credentialResult.getToken()));
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        var request = chain.request();
        var credential = createCredential();

        var newRequest = request.newBuilder()
                .addHeader("Authorization", format("Bearer %s", credential.getToken()))
                .build();
        return chain.proceed(newRequest);
    }

    private TokenRepresentation createCredential() {
        var credentialResult = credentialsProvider.apply(TokenParameters.Builder.newInstance()
                .audience(audience)
                .build());
        if (credentialResult.failed()) {
            throw new RuntimeException("Error creating client credentials: " + credentialResult.getFailureMessages());
        }
        return credentialResult.getContent();
    }
}
