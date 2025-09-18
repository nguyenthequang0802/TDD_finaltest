package repositories;

import models.Order;

import java.util.List;

public interface IOrderRepository extends IBaseRepository<Order> {
    List<Order> findByUserId(long userId);
}
