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

package org.eclipse.edc.registration.store.sql.schema;

/**
 * Provides the mapping with columns, statements with the underlying SQL storage system
 */

public interface ParticipantStatements {

    default String getParticipantTable() {
        return "edc_participant";
    }

    default String getParticipantIdColumn() {
        return "id";
    }

    default String getDidColumn() {
        return "did";
    }

    default String getStateColumn() {
        return "state";
    }

    default String getStateCountColumn() {
        return "state_count";
    }

    default String getStateTimestampColumn() {
        return "state_timestamp";
    }

    default String getErrorDetailColumn() {
        return "error_detail";
    }

    default String getTraceContextColumn() {
        return "trace_context";
    }

    default String getCreatedAtColumn() {
        return "created_at";
    }

    default String getUpdatedAtColumn() {
        return "updated_at";
    }


    /**
     * INSERT clause for participants.
     */
    String getInsertParticipantsTemplate();


    /**
     * Select single participants by DID
     */
    String getSelectParticipantByDidTemplate();

    /**
     * Update statement for participants
     */
    String getUpdateParticipantTemplate();


    /**
     * SELECT clause for all participants.
     */
    String getSelectParticipantTemplate();

    /**
     * SELECT clause for all participants.
     */
    String getSelectParticipantByStateTemplate();

}
