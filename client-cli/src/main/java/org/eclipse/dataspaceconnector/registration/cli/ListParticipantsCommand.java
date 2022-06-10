package org.eclipse.dataspaceconnector.registration.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.dataspaceconnector.registration.client.ApiClient;
import org.eclipse.dataspaceconnector.registration.client.ApiClientFactory;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "list")
class ListParticipantsCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @ParentCommand
    private ParticipantsCommand parent;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        var out = spec.commandLine().getOut();
        MAPPER.writeValue(out, parent.parent.registryApiClient.listParticipants());
        out.println();
        return 0;
    }
}
