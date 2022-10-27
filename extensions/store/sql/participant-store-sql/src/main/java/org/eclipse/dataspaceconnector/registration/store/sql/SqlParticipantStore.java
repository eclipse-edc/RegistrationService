/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
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

package org.eclipse.dataspaceconnector.registration.store.sql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.registration.store.sql.schema.ParticipantStatements;
import org.eclipse.dataspaceconnector.spi.persistence.EdcPersistenceException;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.datasource.DataSourceRegistry;
import org.eclipse.dataspaceconnector.sql.store.AbstractSqlStore;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.sql.SqlQueryExecutor.executeQuery;
import static org.eclipse.dataspaceconnector.sql.SqlQueryExecutor.executeQuerySingle;

/**
 * SQL implementation for {@link ParticipantStore}
 */

public class SqlParticipantStore extends AbstractSqlStore implements ParticipantStore {


    private final ParticipantStatements participantStatements;


    public SqlParticipantStore(DataSourceRegistry dataSourceRegistry, String dataSourceName, TransactionContext transactionContext, ObjectMapper objectMapper, ParticipantStatements participantStatements) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper);
        this.participantStatements = Objects.requireNonNull(participantStatements);
    }

    @Override
    public @Nullable Participant findByDid(String did) {
        Objects.requireNonNull(did);
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                return findByDidInternal(connection, did);
            } catch (EdcPersistenceException e) {
                throw e;
            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });

    }

    @Override
    public List<Participant> listParticipants() {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                try (var stream = executeQuery(connection, true, this::participantMapper, participantStatements.getSelectParticipantTemplate())) {
                    return stream.collect(Collectors.toList());
                }
            } catch (EdcPersistenceException e) {
                throw e;
            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }

    @Override
    public void save(Participant participant) {

        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var existingParticipant = findByDidInternal(connection, participant.getDid());
                if (existingParticipant == null) {
                    insert(connection, participant);
                } else {
                    update(connection, existingParticipant, participant);
                }

            } catch (EdcPersistenceException e) {
                throw e;
            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });

    }

    @Override
    public Collection<Participant> listParticipantsWithStatus(ParticipantStatus state) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                try (var stream = executeQuery(connection, true, this::participantMapper, participantStatements.getSelectParticipantByStateTemplate(), state.code())) {
                    return stream.collect(Collectors.toList());
                }
            } catch (EdcPersistenceException e) {
                throw e;
            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }

    private void update(Connection connection, Participant oldParticipant, Participant participant) {

        if (!oldParticipant.getId().equals(participant.getId())) {
            throw new EdcPersistenceException(format("Failed to update Participant with did %s: participant id didn't match", participant.getDid()));
        }
        executeQuery(connection, participantStatements.getUpdateParticipantTemplate(),
                participant.getState(),
                participant.getStateCount(),
                participant.getStateTimestamp(),
                participant.getErrorDetail(),
                toJson(participant.getTraceContext()),
                participant.getUpdatedAt(),
                participant.getDid()
        );

    }

    private void insert(Connection connection, Participant participant) {
        executeQuery(connection, participantStatements.getInsertParticipantsTemplate(),
                participant.getId(),
                participant.getDid(),
                participant.getState(),
                participant.getStateCount(),
                participant.getStateTimestamp(),
                participant.getErrorDetail(),
                toJson(participant.getTraceContext()),
                participant.getCreatedAt(),
                participant.getUpdatedAt()
        );
    }

    private Participant participantMapper(ResultSet resultSet) throws SQLException {
        return Participant.Builder.newInstance()
                .did(resultSet.getString(participantStatements.getDidColumn()))
                .id(resultSet.getString(participantStatements.getParticipantIdColumn()))
                .traceContext(fromJson(resultSet.getString(participantStatements.getTraceContextColumn()), new TypeReference<>() {
                }))
                .createdAt(resultSet.getLong(participantStatements.getCreatedAtColumn()))
                .updatedAt(resultSet.getLong(participantStatements.getUpdatedAtColumn()))
                .stateTimestamp(resultSet.getLong(participantStatements.getStateTimestampColumn()))
                .state(resultSet.getInt(participantStatements.getStateColumn()))
                .build();
    }

    private Participant findByDidInternal(Connection connection, String did) {
        return executeQuerySingle(connection, false, this::participantMapper, participantStatements.getSelectParticipantByDidTemplate(), did);
    }


}
