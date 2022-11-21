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

package org.eclipse.edc.registration.transform;

import org.eclipse.edc.registration.authority.model.Participant;
import org.eclipse.edc.registration.authority.model.ParticipantStatus;
import org.eclipse.edc.registration.model.ParticipantDto;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.registration.TestUtils.getParticipantDtoFromParticipant;
import static org.eclipse.edc.registration.authority.TestUtils.createParticipant;
import static org.mockito.Mockito.mock;

public class ParticipantToParticipantDtoTransformerTest {

    private final ParticipantToParticipantDtoTransformer transformer = new ParticipantToParticipantDtoTransformer();

    @Test
    void inputOutputType() {
        assertThat(transformer.getInputType()).isEqualTo(Participant.class);
        assertThat(transformer.getOutputType()).isEqualTo(ParticipantDto.class);
    }

    @ParameterizedTest
    @EnumSource(value = ParticipantStatus.class)
    void transform(ParticipantStatus status) {
        var context = mock(TransformerContext.class);
        var participant = createParticipant().status(status).build();
        var expectedParticipantDto = getParticipantDtoFromParticipant(participant);

        var participantDto = transformer.transform(participant, context);

        assertThat(participantDto)
                .usingRecursiveComparison()
                .isEqualTo(expectedParticipantDto);
    }
}
