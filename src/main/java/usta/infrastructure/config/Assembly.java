package usta.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.*;
import usta.domain.model.*;
import usta.domain.service.*;
import usta.application.port.in.*;
import usta.application.port.out.*;
import usta.application.usecase.*;
import usta.infrastructure.adapter.in.json.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import usta.infrastructure.adapter.out.persistence.adapter.JpaAdjustment;
import usta.infrastructure.adapter.out.persistence.adapter.JpaHealthBillingData;
import usta.infrastructure.adapter.out.persistence.adapter.JpaItem;
import usta.infrastructure.adapter.out.persistence.adapter.JpaPayment;

@ApplicationScoped
public class Assembly {
    @Produces ProcessDocuments processing(RequestRepository requests,DocumentRepository documents,EquivalenceRepository mappings,
            BillingJson reader,SysCafeContract contract,Transaction transaction) {
        return new ProcessingService(requests,documents,mappings,new JsonPayloadStructure(),reader,contract,
                new BillingReconciliationService(),transaction,Clock.systemUTC());
    }
    @Produces QueryIntegration queries(RequestRepository requests,DocumentRepository documents,Transaction transaction,
            @ConfigProperty(name="billing.alert-hours") int hours,@ConfigProperty(name="billing.page-size") int size) {
        return new IntegrationQueryService(requests,documents,transaction,Clock.systemUTC(),Duration.ofHours(hours),size);
    }
    // Una sola instancia: sirve como ManageUsers y como AuthenticateUsers
    @Produces @jakarta.inject.Singleton UserService users(UserRepository repo,PasswordHasher hasher,Transaction transaction) {
        return new UserService(repo,hasher,transaction);
    }
    @Produces ManageStagedDocuments stagedDocuments(StagedDocumentStore store, RequestRepository requests,
            SysCafeContract contract, EquivalenceRepository mappings, Transaction transaction) {
        return new StagedDocumentService(store, requests, contract, mappings, transaction, Clock.systemUTC());
    }
    @Produces ManageCustomers customers(CustomerRepository repo) { return new CustomerService(repo); }
    @Produces ManageEquivalences equivalences(EquivalenceRepository repo) { return new EquivalenceService(repo); }
    @Produces ManageDocumentDetails<Item> manageItem(JpaItem repository, Transaction transaction) {
        return new DocumentDetailService<>(Item.class,repository,transaction);
    }
    @Produces ManageDocumentDetails<Payment> managePayment(JpaPayment repository, Transaction transaction) {
        return new DocumentDetailService<>(Payment.class,repository,transaction);
    }
    @Produces ManageDocumentDetails<Adjustment> manageAdjustment(JpaAdjustment repository, Transaction transaction) {
        return new DocumentDetailService<>(Adjustment.class,repository,transaction);
    }
    @Produces ManageDocumentDetails<HealthBillingData> manageHealthBillingData(JpaHealthBillingData repository, Transaction transaction) {
        return new DocumentDetailService<>(HealthBillingData.class,repository,transaction);
    }
}
