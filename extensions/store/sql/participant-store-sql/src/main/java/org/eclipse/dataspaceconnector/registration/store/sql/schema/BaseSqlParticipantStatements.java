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

package org.eclipse.dataspaceconnector.registration.store.sql.schema;

import org.eclipse.dataspaceconnector.sql.dialect.BaseSqlDialect;

import static java.lang.String.format;

/**
 * Provide an agnostic SQL implementation of {@link ParticipantStatements} which is not tied to
 * a particular SQL storage
 */
public class BaseSqlParticipantStatements implements ParticipantStatements {

    @Override
    public String getInsertParticipantsTemplate() {
        return format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?,?,?,?,?,?,?%s,?,?)",
                getParticipantTable(), getParticipantIdColumn(), getDidColumn(), getStateColumn(),
                getStateCountColumn(), getStateTimestampColumn(), getErrorDetailColumn(), getTraceContextColumn(), getCreatedAtColumn(), getUpdatedAtColumn(), getFormatJsonOperator());
    }


    @Override
    public String getSelectParticipantByDidTemplate() {
        return format("SELECT * FROM %s WHERE %s=?", getParticipantTable(), getDidColumn());
    }

    @Override
    public String getUpdateParticipantTemplate() {
        return format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?%s, %s=? WHERE %s = ?;",
                getParticipantTable(), getStateColumn(), getStateCountColumn(), getStateTimestampColumn(),
                getErrorDetailColumn(), getTraceContextColumn(), getFormatJsonOperator(), getUpdatedAtColumn(), getDidColumn());
    }

    @Override
    public String getSelectParticipantTemplate() {
        return format("SELECT * FROM %s", getParticipantTable());
    }

    @Override
    public String getSelectParticipantByStateTemplate() {
        return format("SELECT * FROM %s WHERE %s=?", getParticipantTable(), getStateColumn());
    }

    protected String getFormatJsonOperator() {
        return BaseSqlDialect.getJsonCastOperator();
    }

}
