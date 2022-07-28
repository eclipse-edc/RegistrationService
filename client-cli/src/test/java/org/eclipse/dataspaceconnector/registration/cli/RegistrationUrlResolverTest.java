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

package org.eclipse.dataspaceconnector.registration.cli;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolver;
import org.eclipse.dataspaceconnector.iam.did.web.resolution.WebDidResolver;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationUrlResolverTest {

    private static final String REGISTRATION_URL_TYPE = "RegistrationUrl";
    private static final Faker FAKER = new Faker();

    DidResolver didResolver = mock(WebDidResolver.class);
    RegistrationUrlResolver urlResolver = new RegistrationUrlResolver(didResolver);

    String did = "did:web:" + FAKER.internet().domainName();
    static String apiUrl = FAKER.internet().url();

    @Test
    void resolveUrl_success() {

        DidDocument didDocument = didDocument(List.of(new Service("some-id", REGISTRATION_URL_TYPE, apiUrl), new Service("some-id", "some-other-type", apiUrl)));
        when(didResolver.resolve(did)).thenReturn(Result.success(didDocument));

        Result<String> resultApiUrl = urlResolver.resolveUrl(did);

        assertThat(resultApiUrl.succeeded()).isTrue();
        assertThat(resultApiUrl.getContent()).isEqualTo(apiUrl);

    }

    @ParameterizedTest
    @MethodSource("listsOfServices")
    void resolveUrl_incorrectServices(List<Service> services) {

        DidDocument didDocument = didDocument(services);
        when(didResolver.resolve(did)).thenReturn(Result.success(didDocument));

        Result<String> resultApiUrl = urlResolver.resolveUrl(did);

        assertThat(resultApiUrl.failed()).isTrue();
        assertThat(resultApiUrl.getFailureMessages()).containsExactly("Error resolving service endpoint from DID Document for " + did);
    }

    @Test
    void resolveUrl_failureToGetDid() {

        when(didResolver.resolve(did)).thenReturn(Result.failure("Failure"));

        assertThatThrownBy(() -> urlResolver.resolveUrl(did)).isInstanceOf(CliException.class);
    }

    private DidDocument didDocument(List<Service> services) {
        return DidDocument.Builder.newInstance().service(services).build();
    }

    private static Stream<Arguments> listsOfServices() {
        return Stream.of(
                Arguments.of(List.of(new Service("some-id", "some-other-type", apiUrl))),
                Arguments.of(List.of())
        );
    }

}