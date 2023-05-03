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

package org.eclipse.edc.registration.api;

import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.edc.registration.model.ParticipantDto;
import org.eclipse.edc.registration.spi.registration.RegistrationService;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;
import static org.eclipse.edc.registration.TestUtils.createParticipantDto;
import static org.eclipse.edc.spi.result.Result.failure;
import static org.eclipse.edc.spi.result.Result.success;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class RegistrationServiceApiControllerTest {

    private static final String DID = "some.test/url";

    private final RegistrationService registrationService = mock(RegistrationService.class);
    private final TypeTransformerRegistry transformerRegistry = mock(TypeTransformerRegistry.class);

    private RegistrationServiceApiController controller;

    @BeforeEach
    public void setUp() {
        controller = new RegistrationServiceApiController(registrationService, transformerRegistry);
    }

    @Test
    void listParticipants_empty() {
        when(registrationService.listParticipants()).thenReturn(List.of());

        var participants = controller.listParticipants();

        assertThat(participants).isEmpty();
    }

    @Test
    void listParticipants() {
        var participant = createParticipant().build();
        var participantDto = createParticipantDto().build();
        when(registrationService.listParticipants()).thenReturn(List.of(participant));
        when(transformerRegistry.transform(participant, ParticipantDto.class))
                .thenReturn(success(participantDto));

        var result = controller.listParticipants();

        assertThat(result).containsExactly(participantDto);
        verify(transformerRegistry).transform(participant, ParticipantDto.class);
    }

    @Test
    void listParticipants_verifyResultFilter() {
        var participant1 = createParticipant().build();
        var participant2 = createParticipant().build();
        var participantDto1 = createParticipantDto().build();

        when(registrationService.listParticipants()).thenReturn(List.of(participant1, participant2));
        // Transform for participant1 returns success.
        when(transformerRegistry.transform(participant1, ParticipantDto.class))
                .thenReturn(success(participantDto1));
        // Transform for participant2 returns failure.
        when(transformerRegistry.transform(participant2, ParticipantDto.class))
                .thenReturn(failure("error"));

        var result = controller.listParticipants();

        assertThat(result).containsExactly(participantDto1);
        verify(transformerRegistry).transform(participant1, ParticipantDto.class);
        verify(transformerRegistry).transform(participant2, ParticipantDto.class);
    }

    @Test
    void addParticipant() {
        var header = mock(HttpHeaders.class);
        when(header.getHeaderString("CallerDid")).thenReturn(DID);

        controller.addParticipant(header);

        verify(registrationService).addParticipant(DID);
    }

    @Test
    void findByDid() {
        var participant = createParticipant().build();

        var header = mock(HttpHeaders.class);
        when(header.getHeaderString("CallerDid")).thenReturn(participant.getDid());
        var participantDto = createParticipantDto().build();
        when(registrationService.findByDid(participant.getDid()))
                .thenReturn(participant);
        when(transformerRegistry.transform(participant, ParticipantDto.class))
                .thenReturn(success(participantDto));

        var found = controller.getParticipant(header);

        assertThat(found).usingRecursiveComparison().isEqualTo(participantDto);
        verify(transformerRegistry).transform(participant, ParticipantDto.class);
    }

    @Test
    void findByDid_dtoTransformerFailure() {
        var participant = createParticipant().build();
        var header = mock(HttpHeaders.class);
        when(header.getHeaderString("CallerDid")).thenReturn(participant.getDid());
        when(registrationService.findByDid(participant.getDid())).thenReturn(participant);
        when(transformerRegistry.transform(participant, ParticipantDto.class))
                .thenReturn(failure("error"));

        assertThatExceptionOfType(EdcException.class).isThrownBy(() -> controller.getParticipant(header))
                .withMessage("error");

        verify(transformerRegistry).transform(participant, ParticipantDto.class);
    }

    @Test
    void findByDid_notFound() {
        var participant = createParticipant().build();
        var header = mock(HttpHeaders.class);
        when(header.getHeaderString("CallerDid")).thenReturn(participant.getDid());
        when(registrationService.findByDid(participant.getDid())).thenReturn(null);

        assertThatExceptionOfType(ObjectNotFoundException.class).isThrownBy(() -> controller.getParticipant(header));

        verifyNoInteractions(transformerRegistry);
    }
}
