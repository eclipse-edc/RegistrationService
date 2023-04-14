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

package org.eclipse.edc.registration.client.response;

import org.eclipse.edc.spi.result.AbstractResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ApiResult<T> extends AbstractResult<T, ApiFailure, ApiResult<T>> {

    protected ApiResult(T content, ApiFailure failure) {
        super(content, failure);
    }

    public static <T> ApiResult<T> success(T content) {
        return new ApiResult<>(content, null);
    }

    public static <T> ApiResult<T> conflict(String message) {
        return new ApiResult<>(null, new ApiFailure(List.of(message), 409));
    }

    public static <T> ApiResult<T> notFound(String message) {
        return new ApiResult<>(null, new ApiFailure(List.of(message), 404));
    }

    public static <T> ApiResult<T> badRequest(String... message) {
        return badRequest(List.of(message));
    }

    public static <T> ApiResult<T> badRequest(List<String> messages) {
        return new ApiResult<>(null, new ApiFailure(messages, 400));
    }

    public static <T> ApiResult<T> success() {
        return ApiResult.success(null);
    }

    public static <T> ApiResult<T> failure(int code, String... message) {
        return new ApiResult<>(null, new ApiFailure(List.of(message), code));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R1 extends AbstractResult<C1, ApiFailure, R1>, C1> @NotNull R1 newInstance(@Nullable C1 content, @Nullable ApiFailure failure) {
        return (R1) new ApiResult<>(content, failure);
    }

    public int reason() {
        return getFailure().code();
    }
}
