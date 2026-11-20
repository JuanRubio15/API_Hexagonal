package usta.application.port.in;

import java.util.List;
import usta.domain.model.Customer;

public interface ManageCustomers {
    Customer save(Customer customer);
    Customer get(long id);
    List<Customer> list();
    void delete(long id);
}
