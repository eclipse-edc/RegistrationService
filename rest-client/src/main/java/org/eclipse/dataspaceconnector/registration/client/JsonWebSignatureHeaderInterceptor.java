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

package org.eclipse.dataspaceconnector.registration.client;

import org.eclipse.dataspaceconnector.spi.iam.TokenParameters;
import org.eclipse.dataspaceconnector.spi.iam.TokenRepresentation;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.net.http.HttpRequest;
import java.util.function.Consumer;
import java.util.function.Function;

public class JsonWebSignatureHeaderInterceptor implements Consumer<HttpRequest.Builder> {

    private final Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider;
    private final String audience;

    public JsonWebSignatureHeaderInterceptor(Function<TokenParameters, Result<TokenRepresentation>> credentialsProvider, String audience) {
        this.credentialsProvider = credentialsProvider;
        this.audience = audience;
    }

    @Override
    public void accept(HttpRequest.Builder b) {
        var credentialResult = credentialsProvider.apply(TokenParameters.Builder.newInstance()
                .audience(audience)
                .build());
        if (credentialResult.failed()) {
            throw new RuntimeException("Error creating client credentials: " + credentialResult.getFailureMessages());
        }
        b.header("Authorization", String.format("Bearer %s", credentialResult.getContent().getToken()));
    }
}