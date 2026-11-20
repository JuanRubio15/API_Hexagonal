package usta.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.function.Supplier;
import usta.application.port.out.Transaction;

@ApplicationScoped
public class JtaTransaction implements Transaction {
    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public <T> T run(Supplier<T> work) {
        return work.get();
    }
}
