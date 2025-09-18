package services;

import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.IBookRepository;
import repositories.ICartRepository;
import repositories.IOrderRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    IOrderRepository orderRepository;
    @Mock
    IBookRepository bookRepository;
    @Mock
    ICartRepository cartRepository;

    @InjectMocks
    OrderService orderService;

    @Test
    void should_create_order_and_reduce_stock_when_checkout_cart() {
        long cartId = 1L;
        long bookId = 100L;
        int stock = 5;
        int quantity = 2;

        User user = User.builder().id(1L).build();
        Book book = Book.builder().id(bookId).stockQuantity(stock).build();
        CartItem cartItem = CartItem.builder().id(10L).book(book).quantity(quantity).build();
        Cart cart = Cart.builder().id(cartId).user(user).cartItem(cartItem).build();

        when(cartRepository.findById(cartId)).thenReturn(cart);

        when(orderRepository.create(any(Order.class)))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    o.setId(999L);
                    return o;
                });

        long orderId = orderService.checkoutCart(cartId);

        assertThat(orderId, equalTo(999L));
        assertThat(book.getStockQuantity(), equalTo(3)); // 5 - 2
        verify(bookRepository).update(book.getId(), book);
        verify(orderRepository).create(any(Order.class));
        verify(cartRepository).delete(cartId);
    }

    @Test
    void should_throw_exception_when_cart_not_found() {
        long cartId = 1L;

        when(cartRepository.findById(cartId)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.checkoutCart(cartId)
        );

        assertThat(ex.getMessage(), equalTo("Cart not found"));

        verify(cartRepository, times(1)).findById(cartId);
        verifyNoInteractions(orderRepository, bookRepository);
    }

    @Test
    void should_throw_exception_when_cart_is_empty() {
        long cartId = 1L;
        Cart cart = Cart.builder().id(cartId).user(new User()).cartItem(null).build();

        when(cartRepository.findById(cartId)).thenReturn(cart);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> orderService.checkoutCart(cartId)
        );

        assertThat(ex.getMessage(), equalTo("Cart is empty"));

        verify(cartRepository, times(1)).findById(cartId);
        verifyNoInteractions(orderRepository, bookRepository);
    }

    @Test
    void should_throw_exception_when_not_enough_stock() {
        long cartId = 1L;
        Book book = Book.builder().id(100L).stockQuantity(1).build(); // chỉ còn 1
        CartItem cartItem = CartItem.builder().id(10L).book(book).quantity(5).build(); // cần 5
        Cart cart = Cart.builder().id(cartId).user(new User()).cartItem(cartItem).build();

        when(cartRepository.findById(cartId)).thenReturn(cart);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> orderService.checkoutCart(cartId)
        );

        assertThat(ex.getMessage(), equalTo("Not enough stock"));

        verify(cartRepository, times(1)).findById(cartId);
        verifyNoInteractions(orderRepository, bookRepository);
    }

    @Test
    void should_update_book_stock_after_checkout() {
        long cartId = 1L;
        Book book = Book.builder().id(200L).stockQuantity(10).build();
        CartItem cartItem = CartItem.builder().id(20L).book(book).quantity(4).build();
        Cart cart = Cart.builder().id(cartId).user(new User()).cartItem(cartItem).build();

        when(cartRepository.findById(cartId)).thenReturn(cart);
        when(orderRepository.create(any(Order.class)))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    o.setId(300L);
                    return o;
                });

        orderService.checkoutCart(cartId);

        assertThat(book.getStockQuantity(), equalTo(6));
        verify(bookRepository).update(book.getId(), book);
    }

    @Test
    void should_restore_stock_and_delete_order_when_cancel() {
        long orderId = 100L;
        Book book = Book.builder().id(1L).stockQuantity(3).build();
        Order order = Order.builder().id(orderId).book(book).quantity(2).build();

        when(orderRepository.findById(orderId)).thenReturn(order);

        orderService.cancelOrder(orderId);

        assertThat(book.getStockQuantity(), equalTo(5)); // 3 + 2
        verify(bookRepository).update(book.getId(), book);
        verify(orderRepository).delete(orderId);
    }

    @Test
    void should_throw_exception_when_cancel_non_existing_order() {
        long orderId = 200L;
        when(orderRepository.findById(orderId)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelOrder(orderId)
        );

        verify(orderRepository, never()).delete(anyLong());
        verify(bookRepository, never()).update(anyLong(), any(Book.class));
    }

    @Test
    void should_return_orders_when_user_has_orders() {
        long userId = 1L;
        Book book = Book.builder().id(10L).title("Book A").stockQuantity(5).build();

        Order order1 = Order.builder().id(100L).book(book).quantity(2).build();
        Order order2 = Order.builder().id(101L).book(book).quantity(1).build();

        when(orderRepository.findByUserId(userId)).thenReturn(Arrays.asList(order1, order2));

        List<Order> orders = orderService.viewOrders(userId);

        assertThat(orders.size(), equalTo(2));
        assertThat(orders.get(0).getId(), equalTo(100L));
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void should_return_empty_list_when_user_has_no_orders() {
        long userId = 2L;
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Order> orders = orderService.viewOrders(userId);

        assertThat(orders.isEmpty(), equalTo(true));
        verify(orderRepository).findByUserId(userId);
    }
}
