package org.eclipse.edc.registration.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Dataspace Participant DTO
 */
public class ParticipantDto {
    public static final String JSON_PROPERTY_DID = "did";
    public static final String JSON_PROPERTY_STATUS = "status";
    private String did;
    private OnboardingStatus status;

    public ParticipantDto() {
    }

    public ParticipantDto(@JsonProperty(JSON_PROPERTY_DID) String did,
                          @JsonProperty(JSON_PROPERTY_STATUS) OnboardingStatus status) {
        this.did = did;
        this.status = status;
    }


    @JsonProperty(JSON_PROPERTY_DID)
    public String getDid() {
        return did;
    }

    @JsonProperty(JSON_PROPERTY_STATUS)
    public OnboardingStatus getStatus() {
        return status;
    }

    /**
     * Participant onboarding status DTO
     */
    public enum OnboardingStatus {
        ONBOARDING_IN_PROGRESS("ONBOARDING_IN_PROGRESS"),

        ONBOARDED("ONBOARDED"),

        DENIED("DENIED");

        private String value;

        OnboardingStatus(String value) {
            this.value = value;
        }

        @JsonCreator
        public static OnboardingStatus fromValue(String value) {
            for (OnboardingStatus b : OnboardingStatus.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

}

