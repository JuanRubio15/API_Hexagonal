package usta.infrastructure.adapter.in.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.*;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import java.nio.file.*;
import java.util.Scanner;
import usta.application.port.in.*;
import usta.infrastructure.adapter.in.Maintenance;
import usta.infrastructure.adapter.in.json.BillingJson;

@QuarkusMain
public class BillingMain implements QuarkusApplication {
    @Inject ProcessDocuments processing;
    @Inject QueryIntegration queries;
    @Inject Maintenance maintenance;
    @Inject BillingJson json;
    @Inject ObjectMapper mapper;

    public int run(String... args) throws Exception {
        if (args.length == 0 || !"console".equals(args[0])) {
            Quarkus.waitForExit();
            return 0;
        }
        try (var input = new Scanner(System.in)) {
            System.out.println("Comandos: receive ARCHIVO | retry ID | requests | history ID | alerts | documents");
            System.out.println("CRUD: list RECURSO | get RECURSO ID | create RECURSO ARCHIVO | update RECURSO ID ARCHIVO | delete RECURSO ID");
            System.out.println("Recursos: " + Maintenance.RESOURCES + ". Escriba exit para salir.");
            while (input.hasNextLine()) {
                String line = input.nextLine().strip();
                if (line.equals("exit")) break;
                if (line.isBlank()) continue;
                try { System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(execute(line))); }
                catch (RuntimeException | java.io.IOException e) { System.err.println("Error: "+e.getMessage()); }
            }
        }
        return 0;
    }

    private Object execute(String line) throws java.io.IOException {
        String[] args = line.split("\\s+",4);
        return switch(args[0]) {
            case "requests" -> queries.requests();
            case "documents" -> queries.documents();
            case "alerts" -> queries.alerts();
            case "history" -> queries.history(Long.parseLong(args[1]));
            case "retry" -> processing.retry(Long.parseLong(args[1]),"consola");
            case "receive" -> processing.receive(json.receive(Files.readString(Path.of(line.substring(8).strip())),"consola"));
            case "list" -> maintenance.list(args[1]);
            case "get" -> maintenance.get(args[1],Long.parseLong(args[2]));
            case "create" -> maintenance.save(args[1],null,json.parse(Files.readString(Path.of(line.split("\\s+",3)[2]))));
            case "update" -> maintenance.save(args[1],Long.valueOf(args[2]),json.parse(Files.readString(Path.of(args[3]))));
            case "delete" -> { maintenance.delete(args[1],Long.parseLong(args[2])); yield "Eliminado"; }
            default -> throw new IllegalArgumentException("Comando desconocido.");
        };
    }
}
