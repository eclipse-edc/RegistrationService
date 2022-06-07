package org.eclipse.dataspaceconnector.registration.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataspace participant.
 */
@JsonDeserialize(builder = Participant.Builder.class)
public class Participant {

    private String name;
    private String url;
    private final List<String> supportedProtocols = new ArrayList<>();

    private Participant() {
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final Participant policy;

        private Builder() {
            policy = new Participant();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder supportedProtocol(String supportedProtocol) {
            policy.supportedProtocols.add(supportedProtocol);
            return this;
        }

        public Builder supportedProtocols(List<String> supportedProtocols) {
            policy.supportedProtocols.addAll(supportedProtocols);
            return this;
        }

        public Builder url(String url) {
            policy.url = url;
            return this;
        }

        public Builder name(String name) {
            policy.name = name;
            return this;
        }

        public Participant build() {
            return policy;
        }
    }
}
