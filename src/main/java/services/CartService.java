package services;

import models.Book;
import models.Cart;
import models.CartItem;
import models.User;
import org.springframework.stereotype.Service;
import repositories.IBookRepository;
import repositories.ICartItemRepository;
import repositories.ICartRepository;
import repositories.IUserRepository;

@Service
public class CartService {
    private final IUserRepository userRepository;
    private final IBookRepository bookRepository;
    private final ICartItemRepository cartItemRepository;
    private final ICartRepository cartRepository;

    public CartService(IUserRepository userRepository, IBookRepository bookRepository, ICartItemRepository cartItemRepository, ICartRepository cartRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
    }

    public long createCart(long userId, long bookId, int quantity) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Book book = bookRepository.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found");
        }

        // nếu quantity > stock thì giới hạn
        int finalQuantity = Math.min(quantity, book.getStockQuantity());

        CartItem cartItem = CartItem.builder()
                .id(1L) // trong thực tế id sẽ do DB sinh
                .book(book)
                .quantity(finalQuantity)
                .build();
        cartItemRepository.create(cartItem);

        Cart cart = Cart.builder()
                .id(1L) // cũng thường do DB sinh
                .user(user)
                .cartItem(cartItem)
                .build();
        cartRepository.create(cart);

        return cart.getId();
    }

    public Cart viewCart(long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for userId: " + userId);
        }
        return cart;
    }
}
