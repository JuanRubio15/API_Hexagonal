package usta.application.port.in;

import java.util.List;
import usta.domain.model.Equivalence;

public interface ManageEquivalences {
    List<Equivalence> list();
    Equivalence get(long id);
    Equivalence save(Equivalence mapping);
    void delete(long id);
}
