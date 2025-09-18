package repositories;

import models.Cart;

public interface ICartRepository extends IBaseRepository<Cart> {
    Cart findByUserId(long userId);
}
