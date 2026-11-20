package usta.application.port.out;

import java.util.List;
import usta.domain.model.Customer;

public interface CustomerRepository {
    Customer save(Customer customer);
    Customer get(long id);
    List<Customer> list();
    /** Atómico. Devuelve false si existen documentos asociados, y distingue un ID inexistente. */
    boolean deleteIfUnused(long id);
}
