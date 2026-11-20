package usta.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import usta.application.port.in.ManageCustomers;
import usta.application.port.out.CustomerRepository;
import usta.domain.model.Customer;

@RequiredArgsConstructor
public final class CustomerService implements ManageCustomers {
    private final CustomerRepository customers;
    public Customer save(Customer customer) { return customers.save(customer); }
    public Customer get(long id) { return customers.get(id); }
    public List<Customer> list() { return customers.list(); }
    public void delete(long id) {
        if (!customers.deleteIfUnused(id)) throw new IllegalStateException("El cliente tiene documentos asociados.");
    }
}
