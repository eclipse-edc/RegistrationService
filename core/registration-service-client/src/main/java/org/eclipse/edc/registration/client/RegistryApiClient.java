/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.registration.client;


import org.eclipse.edc.registration.client.model.ParticipantDto;
import org.eclipse.edc.registration.client.response.ApiResult;

import java.util.List;

/**
 * Client to access the REST API of the RegistrationService
 */
public interface RegistryApiClient {

    /**
     * Adds (=onboards) a participant to the registry. Note that the identity of the participant is specified in the request headres.
     *
     * @return {@link ApiResult#success()} if the onboarding was initiated successfully, a failed result otherwise.
     */
    ApiResult<Void> addParticipant();

    /**
     * Lists all participants that are currently registered in the dataspace
     */
    ApiResult<List<ParticipantDto>> listParticipants();

    /**
     * Obtains one particular participant identified by a particular DID (transmitted in the header).
     */
    ApiResult<ParticipantDto> getParticipant();

    /**
     * Updates the base URI of the host serving the registration service API.
     */
    void updateBaseUri(String uri);

}
