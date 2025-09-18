package services;

import models.Book;
import models.Cart;
import models.CartItem;
import models.Order;
import org.springframework.stereotype.Service;
import repositories.IBookRepository;
import repositories.ICartRepository;
import repositories.IOrderRepository;

import java.util.List;

@Service
public class OrderService {
    private final ICartRepository cartRepository;
    private final IOrderRepository orderRepository;
    private final IBookRepository bookRepository;

    public OrderService(ICartRepository cartRepository,
                        IOrderRepository orderRepository,
                        IBookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    public long checkoutCart(long cartId) {
        Cart cart = cartRepository.findById(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found");
        }

        CartItem cartItem = cart.getCartItem();
        if (cartItem == null) {
            throw new IllegalStateException("Cart is empty");
        }

        Book book = cartItem.getBook();
        int quantity = cartItem.getQuantity();

        if (book.getStockQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock");
        }

        // trừ stock
        book.setStockQuantity(book.getStockQuantity() - quantity);
        bookRepository.update(book.getId(), book);

        // tạo order
        Order order = new Order();
        order.setBook(book);
        order.setQuantity(quantity);
        Order savedOrder = orderRepository.create(order);

        // cart có thể xóa hoặc đánh dấu đã xử lý
        cartRepository.delete(cartId);

        return savedOrder.getId();
    }

    public void cancelOrder(long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        Book book = order.getBook();
        // cộng lại số lượng vào kho
        book.setStockQuantity(book.getStockQuantity() + order.getQuantity());
        bookRepository.update(book.getId(), book);

        // xóa order
        orderRepository.delete(orderId);
    }

    public List<Order> viewOrders(long userId) {
        return orderRepository.findByUserId(userId);
    }
}
