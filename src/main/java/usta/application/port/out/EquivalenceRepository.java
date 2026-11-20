package usta.application.port.out;

import java.util.List;
import usta.domain.model.Equivalence;

public interface EquivalenceRepository {
    List<Equivalence> list();
    Equivalence get(long id);
    boolean supports(String category, String sourceField, String targetField);
    /** Alta o edición por ID. Garantiza unicidad incluso con escrituras concurrentes. */
    Equivalence saveUnique(Equivalence mapping);
    void delete(long id);
}
