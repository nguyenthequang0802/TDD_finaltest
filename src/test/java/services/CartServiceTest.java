package services;

import models.Book;
import models.Cart;
import models.CartItem;
import models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.IBookRepository;
import repositories.ICartItemRepository;
import repositories.ICartRepository;
import repositories.IUserRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    IUserRepository userRepository;

    @Mock
    IBookRepository bookRepository;

    @Mock
    ICartRepository cartRepository;

    @Mock
    ICartItemRepository cartItemRepository;

    @InjectMocks
    CartService cartService;

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1"
    })
    public void should_return_cartId_id_adding_book_to_cart_item(long userId, long bookId, int quantity) {
        User user = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(user);

        Book book = Book.builder().id(bookId).stockQuantity(quantity).build();
        when(bookRepository.findById(bookId)).thenReturn(book);

        CartItem cartItem = CartItem.builder()
                .quantity(quantity)
                .book(book)
                .quantity(quantity)
                .build();

        when(cartItemRepository.create(any(CartItem.class))).thenReturn(cartItem);

        Cart newCart = Cart.builder()
                .id(1L)
                .cartItem(cartItem)
                .user(user)
                .build();
        when(cartRepository.create(any(Cart.class))).thenReturn(newCart);

        long cartId = cartService.createCart(userId, bookId, quantity);

        assertThat(cartId, equalTo(1L));

        verify(userRepository, times(1)).findById(userId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(cartItemRepository, times(1)).create(any(CartItem.class));
        verify(cartRepository, times(1)).create(any(Cart.class));
    }

    @Test
    public void should_throw_exception_when_user_not_found() {
        long userId = 1L;
        long bookId = 99L;
        when(userRepository.findById(anyLong())).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.createCart(userId, bookId, 1)
        );

        assertThat(ex.getMessage(), equalTo("User not found"));
        verify(userRepository, times(1)).findById(userId);
        verifyNoInteractions(bookRepository, cartItemRepository, cartRepository);
    }

    @Test
    void should_throw_exception_when_book_not_found() {
        long userId = 1L;
        long bookId = 99L;

        when(userRepository.findById(userId)).thenReturn(User.builder().id(userId).build());
        when(bookRepository.findById(bookId)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.createCart(userId, bookId, 1)
        );

        assertThat(ex.getMessage(), equalTo("Book not found"));
        verify(userRepository, times(1)).findById(userId);
        verify(bookRepository, times(1)).findById(bookId);
        verifyNoInteractions(cartItemRepository, cartRepository);
    }

    @Test
    void should_limit_quantity_to_stock_if_request_is_greater() {
        long userId = 1L;
        long bookId = 1L;

        User user = User.builder().id(userId).build();
        Book book = Book.builder().id(bookId).stockQuantity(3).build();

        when(userRepository.findById(userId)).thenReturn(user);
        when(bookRepository.findById(bookId)).thenReturn(book);

        when(cartItemRepository.create(any(CartItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.create(any(Cart.class)))
                .thenAnswer(inv -> {
                    Cart c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        long cartId = cartService.createCart(userId, bookId, 10); // yêu cầu 10 nhưng stock chỉ 3

        assertThat(cartId, equalTo(1L));
        verify(cartItemRepository).create(argThat(item -> item.getQuantity() == 3));
    }

    @Test
    void should_return_cart_when_user_has_cart() {
        long userId = 1L;
        User user = User.builder().id(userId).build();
        Book book = Book.builder().id(10L).stockQuantity(5).build();
        CartItem cartItem = CartItem.builder().id(100L).book(book).quantity(2).build();
        Cart cart = Cart.builder().id(1L).user(user).cartItem(cartItem).build();

        when(cartRepository.findByUserId(userId)).thenReturn(cart);

        Cart result = cartService.viewCart(userId);

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getUser().getId(), equalTo(userId));
        assertThat(result.getCartItem().getQuantity(), equalTo(2));
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void should_throw_exception_when_user_has_no_cart() {
        long userId = 2L;
        when(cartRepository.findByUserId(userId)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.viewCart(userId)
        );

        verify(cartRepository).findByUserId(userId);
    }
}
