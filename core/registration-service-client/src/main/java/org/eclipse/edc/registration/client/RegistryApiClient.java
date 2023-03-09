package org.eclipse.edc.registration.client;


import org.eclipse.edc.registration.client.model.ParticipantDto;
import org.eclipse.edc.registration.client.response.ApiResult;

import java.util.List;

public interface RegistryApiClient {

    ApiResult<Void> addParticipant();

    ApiResult<List<ParticipantDto>> listParticipants();

    ApiResult<ParticipantDto> getParticipant();

    void updateBaseUri(String uri);

}
