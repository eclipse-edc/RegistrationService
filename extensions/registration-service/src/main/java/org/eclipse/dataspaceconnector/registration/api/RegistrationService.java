package org.eclipse.dataspaceconnector.registration.api;

import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.util.List;

public interface RegistrationService {
    List<Participant> listParticipants();

    Result<Void> addParticipant(String did, String idsUrl);
}
