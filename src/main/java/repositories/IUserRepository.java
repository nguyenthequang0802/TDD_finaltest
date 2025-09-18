package repositories;

import models.User;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends IBaseRepository<User> {
    User findByEmail(String email);
}
