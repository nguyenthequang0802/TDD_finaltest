package repositories;

import org.springframework.stereotype.Repository;

@Repository
public interface IBaseRepository<Model> {
    Model findById(long id);
    Model create(Model model);
    Model update(long id, Model model);
    void delete(long id);
}
