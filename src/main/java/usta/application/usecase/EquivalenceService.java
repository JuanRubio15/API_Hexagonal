package usta.application.usecase;

import java.util.List;
import usta.application.port.in.ManageEquivalences;
import usta.application.port.out.EquivalenceRepository;
import usta.domain.model.Equivalence;

public final class EquivalenceService implements ManageEquivalences {
    private final EquivalenceRepository repository;

    public EquivalenceService(EquivalenceRepository repository) { this.repository = repository; }

    public List<Equivalence> list() { return repository.list(); }
    public Equivalence get(long id) { return repository.get(id); }
    public void delete(long id) { repository.delete(id); }

    public Equivalence save(Equivalence mapping) {
        if (!repository.supports(mapping.getCategory(), mapping.getSourceField(), mapping.getTargetField())) {
            throw new IllegalArgumentException("Los campos no pertenecen a la categoría elegida.");
        }
        return repository.saveUnique(mapping);
    }
}
