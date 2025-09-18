package services;

import models.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.IBookRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    IBookRepository bookRepository;

    @InjectMocks
    BookService bookService;

    @Test
    public void should_return_book_when_id_exists() {
        long bookId = 1L;
        Book expectedBook = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(10.0)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(expectedBook);

        Book result = bookService.getBookById(bookId);

        assertThat(result, equalTo(expectedBook));
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    public void should_throw_exception_when_book_id_not_found() {
        // Arrange
        long bookId = 99L;
        when(bookRepository.findById(bookId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.getBookById(bookId)
        );

        assertThat(exception.getMessage(), equalTo("Book not found with id: " + bookId));
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    public void should_return_book_when_negative_id_exists() {
        // Arrange
        long negativeBookId = -1L;
        Book expectedBook = Book.builder()
                .id(negativeBookId)
                .title("Negative ID Book")
                .author("Test Author")
                .price(15.0)
                .build();

        when(bookRepository.findById(negativeBookId)).thenReturn(expectedBook);

        // Act
        Book result = bookService.getBookById(negativeBookId);

        // Assert
        assertThat(result, equalTo(expectedBook));
        verify(bookRepository, times(1)).findById(negativeBookId);
    }

    @Test
    public void should_return_book_when_zero_id_exists() {
        // Arrange
        long zeroBookId = 0L;
        Book expectedBook = Book.builder()
                .id(zeroBookId)
                .title("Zero ID Book")
                .author("Test Author")
                .price(20.0)
                .build();

        when(bookRepository.findById(zeroBookId)).thenReturn(expectedBook);

        // Act
        Book result = bookService.getBookById(zeroBookId);

        // Assert
        assertThat(result, equalTo(expectedBook));
        verify(bookRepository, times(1)).findById(zeroBookId);
    }

    @Test
    public void should_return_book_when_large_id_exists() {
        // Arrange
        long largeBookId = Long.MAX_VALUE;
        Book expectedBook = Book.builder()
                .id(largeBookId)
                .title("Large ID Book")
                .author("Test Author")
                .price(25.0)
                .build();

        when(bookRepository.findById(largeBookId)).thenReturn(expectedBook);

        // Act
        Book result = bookService.getBookById(largeBookId);

        // Assert
        assertThat(result, equalTo(expectedBook));
        verify(bookRepository, times(1)).findById(largeBookId);
    }

    @Test
    public void should_create_book_with_valid_data() {
        // Arrange
        Book bookToCreate = Book.builder()
                .title("New Book")
                .author("New Author")
                .price(15.0)
                .stockQuantity(10)
                .build();

        Book createdBook = Book.builder()
                .id(1L)
                .title("New Book")
                .author("New Author")
                .price(15.0)
                .stockQuantity(10)
                .build();

        when(bookRepository.create(bookToCreate)).thenReturn(createdBook);

        // Act
        Book result = bookService.createBook(bookToCreate);

        // Assert
        assertThat(result, equalTo(createdBook));
        verify(bookRepository, times(1)).create(bookToCreate);
    }

    @ParameterizedTest
    @CsvSource({
            "'', Author, 10.0, Title cannot be empty",
            "Title, '', 10.0, Author cannot be empty",
            "Title, Author, 0.0, Price must be greater than 0",
            "Title, Author, -1.0, Price must be greater than 0"
    })
    public void should_throw_exception_when_creating_book_with_invalid_data(
            String title, String author, double price, String expectedErrorMessage) {
        // Arrange
        Book invalidBook = Book.builder()
                .title(title)
                .author(author)
                .price(price)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.createBook(invalidBook)
        );

        assertTrue(exception.getMessage().contains(expectedErrorMessage));
        verify(bookRepository, never()).create(any(Book.class));
    }

    @Test
    public void should_throw_exception_when_creating_book_with_null_title() {
        // Arrange
        Book invalidBook = Book.builder()
                .title(null)
                .author("Valid Author")
                .price(10.0)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.createBook(invalidBook)
        );

        assertThat(exception.getMessage(), equalTo("Title cannot be empty"));
        verify(bookRepository, never()).create(any(Book.class));
    }

    @Test
    public void should_throw_exception_when_creating_book_with_null_author() {
        // Arrange
        Book invalidBook = Book.builder()
                .title("Valid Title")
                .author(null)
                .price(10.0)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.createBook(invalidBook)
        );

        assertThat(exception.getMessage(), equalTo("Author cannot be empty"));
        verify(bookRepository, never()).create(any(Book.class));
    }

    @Test
    public void should_throw_exception_when_creating_book_with_null_book_object() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> bookService.createBook(null)
        );

        verify(bookRepository, never()).create(any(Book.class));
    }

    @Test
    public void should_create_book_with_zero_stock_quantity() {
        // Arrange
        Book bookToCreate = Book.builder()
                .title("Book with Zero Stock")
                .author("Test Author")
                .price(10.0)
                .stockQuantity(0)
                .build();

        Book createdBook = Book.builder()
                .id(1L)
                .title("Book with Zero Stock")
                .author("Test Author")
                .price(10.0)
                .stockQuantity(0)
                .build();

        when(bookRepository.create(bookToCreate)).thenReturn(createdBook);

        // Act
        Book result = bookService.createBook(bookToCreate);

        // Assert
        assertThat(result, equalTo(createdBook));
        verify(bookRepository, times(1)).create(bookToCreate);
    }

    @Test
    public void should_create_book_with_minimal_valid_price() {
        // Arrange
        Book bookToCreate = Book.builder()
                .title("Cheap Book")
                .author("Budget Author")
                .price(0.01) // Minimal valid price
                .stockQuantity(5)
                .build();

        Book createdBook = Book.builder()
                .id(1L)
                .title("Cheap Book")
                .author("Budget Author")
                .price(0.01)
                .stockQuantity(5)
                .build();

        when(bookRepository.create(bookToCreate)).thenReturn(createdBook);

        // Act
        Book result = bookService.createBook(bookToCreate);

        // Assert
        assertThat(result, equalTo(createdBook));
        verify(bookRepository, times(1)).create(bookToCreate);
    }

    @Test
    public void should_update_book_when_id_exists() {
        // Arrange
        long bookId = 1L;
        Book existingBook = Book.builder()
                .id(bookId)
                .title("Old Title")
                .author("Old Author")
                .price(10.0)
                .build();

        Book bookToUpdate = Book.builder()
                .title("Updated Title")
                .author("Updated Author")
                .price(15.0)
                .build();

        Book updatedBook = Book.builder()
                .id(bookId)
                .title("Updated Title")
                .author("Updated Author")
                .price(15.0)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(existingBook);
        when(bookRepository.update(bookId, bookToUpdate)).thenReturn(updatedBook);

        // Act
        Book result = bookService.updateBook(bookId, bookToUpdate);

        // Assert
        assertThat(result, equalTo(updatedBook));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(bookId, bookToUpdate);
    }

    @Test
    public void should_throw_exception_when_updating_nonexistent_book() {
        // Arrange
        long bookId = 99L;
        Book bookToUpdate = Book.builder()
                .title("Updated Title")
                .author("Updated Author")
                .price(15.0)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.updateBook(bookId, bookToUpdate)
        );

        assertThat(exception.getMessage(), equalTo("Book not found with id: " + bookId));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).update(anyLong(), any(Book.class));
    }

    @Test
    public void should_update_book_with_negative_id() {
        // Arrange
        long negativeBookId = -1L;
        Book existingBook = Book.builder()
                .id(negativeBookId)
                .title("Existing Book")
                .author("Existing Author")
                .price(20.0)
                .build();

        Book bookToUpdate = Book.builder()
                .title("Updated Title")
                .author("Updated Author")
                .price(25.0)
                .build();

        Book updatedBook = Book.builder()
                .id(negativeBookId)
                .title("Updated Title")
                .author("Updated Author")
                .price(25.0)
                .build();

        when(bookRepository.findById(negativeBookId)).thenReturn(existingBook);
        when(bookRepository.update(negativeBookId, bookToUpdate)).thenReturn(updatedBook);

        // Act
        Book result = bookService.updateBook(negativeBookId, bookToUpdate);

        // Assert
        assertThat(result, equalTo(updatedBook));
        verify(bookRepository, times(1)).findById(negativeBookId);
        verify(bookRepository, times(1)).update(negativeBookId, bookToUpdate);
    }

    @Test
    public void should_delete_book_when_id_exists() {
        // Arrange
        long bookId = 1L;
        Book existingBook = Book.builder()
                .id(bookId)
                .title("Book to Delete")
                .author("Author")
                .price(10.0)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(existingBook);

        // Act
        bookService.deleteBook(bookId);

        // Assert
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).delete(bookId);
    }

    @Test
    public void should_throw_exception_when_deleting_nonexistent_book() {
        // Arrange
        long bookId = 99L;
        when(bookRepository.findById(bookId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.deleteBook(bookId)
        );

        assertThat(exception.getMessage(), equalTo("Book not found with id: " + bookId));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).delete(anyLong());
    }

    @Test
    public void should_delete_book_with_negative_id() {
        // Arrange
        long negativeBookId = -10L;
        Book existingBook = Book.builder()
                .id(negativeBookId)
                .title("Book to Delete")
                .author("Author")
                .price(10.0)
                .build();

        when(bookRepository.findById(negativeBookId)).thenReturn(existingBook);

        // Act
        bookService.deleteBook(negativeBookId);

        // Assert
        verify(bookRepository, times(1)).findById(negativeBookId);
        verify(bookRepository, times(1)).delete(negativeBookId);
    }

    @Test
    public void should_delete_book_with_zero_id() {
        // Arrange
        long zeroBookId = 0L;
        Book existingBook = Book.builder()
                .id(zeroBookId)
                .title("Book to Delete")
                .author("Author")
                .price(10.0)
                .build();

        when(bookRepository.findById(zeroBookId)).thenReturn(existingBook);

        // Act
        bookService.deleteBook(zeroBookId);

        // Assert
        verify(bookRepository, times(1)).findById(zeroBookId);
        verify(bookRepository, times(1)).delete(zeroBookId);
    }

    @Test
    public void should_search_books_with_valid_keyword() {
        // Arrange
        String keyword = "Java";
        List<Book> expectedBooks = Arrays.asList(
                Book.builder().id(1L).title("Java Programming").author("Author 1").price(20.0).build(),
                Book.builder().id(2L).title("Advanced Java").author("Author 2").price(25.0).build()
        );

        when(bookRepository.searchByKeyword(keyword)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.searchBooks(keyword);

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result, equalTo(expectedBooks));
        verify(bookRepository, times(1)).searchByKeyword(keyword);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  \t\n"})
    public void should_throw_exception_when_searching_with_empty_keyword(String emptyKeyword) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.searchBooks(emptyKeyword)
        );

        assertThat(exception.getMessage(), equalTo("Search keyword cannot be empty"));
        verify(bookRepository, never()).searchByKeyword(anyString());
    }

    @Test
    public void should_throw_exception_when_searching_with_null_keyword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.searchBooks(null)
        );

        assertThat(exception.getMessage(), equalTo("Search keyword cannot be empty"));
        verify(bookRepository, never()).searchByKeyword(anyString());
    }

    @Test
    public void should_return_empty_list_when_no_books_found() {
        // Arrange
        String keyword = "nonexistent";
        when(bookRepository.searchByKeyword(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<Book> result = bookService.searchBooks(keyword);

        // Assert
        assertThat(result, hasSize(0));
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).searchByKeyword(keyword);
    }

    @Test
    public void should_search_books_with_special_characters() {
        // Arrange
        String keyword = "C++/Java Programming!@#$%";
        List<Book> expectedBooks = Arrays.asList(
                Book.builder().id(1L).title("C++ Programming Guide").author("Tech Author").price(30.0).build()
        );

        when(bookRepository.searchByKeyword(keyword)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.searchBooks(keyword);

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(expectedBooks));
        verify(bookRepository, times(1)).searchByKeyword(keyword);
    }

    @Test
    public void should_search_books_with_numeric_keyword() {
        // Arrange
        String keyword = "2024";
        List<Book> expectedBooks = Arrays.asList(
                Book.builder().id(1L).title("Programming Trends 2024").author("Future Author").price(25.0).build()
        );

        when(bookRepository.searchByKeyword(keyword)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.searchBooks(keyword);

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result, equalTo(expectedBooks));
        verify(bookRepository, times(1)).searchByKeyword(keyword);
    }

    @Test
    public void should_search_books_with_long_keyword() {
        // Arrange
        String longKeyword = "This is a very long keyword that contains multiple words and should still work perfectly for searching books in the system";
        List<Book> expectedBooks = Collections.emptyList();

        when(bookRepository.searchByKeyword(longKeyword)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.searchBooks(longKeyword);

        // Assert
        assertThat(result, hasSize(0));
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).searchByKeyword(longKeyword);
    }

    @Test
    public void should_update_stock_successfully() {
        // Arrange
        long bookId = 1L;
        int initialStock = 10;
        int quantityToAdd = 5;

        Book book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(bookId, quantityToAdd);

        // Assert
        assertTrue(result);
        assertThat(book.getStockQuantity(), equalTo(initialStock + quantityToAdd));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(eq(bookId), any(Book.class));
    }

    @Test
    public void should_return_false_when_reducing_stock_below_zero() {
        // Arrange
        long bookId = 1L;
        int initialStock = 5;
        int quantityToRemove = -10; // Trying to remove 10 books

        Book book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(bookId, quantityToRemove);

        // Assert
        assertFalse(result);
        assertThat(book.getStockQuantity(), equalTo(initialStock)); // Stock should remain unchanged
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).update(anyLong(), any(Book.class));
    }

    @Test
    public void should_throw_exception_when_updating_stock_for_nonexistent_book() {
        // Arrange
        long bookId = 99L;
        when(bookRepository.findById(bookId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.updateStock(bookId, 5)
        );

        assertThat(exception.getMessage(), equalTo("Book not found with id: " + bookId));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).update(anyLong(), any(Book.class));
    }

    @Test
    public void should_update_stock_with_zero_quantity() {
        // Arrange
        long bookId = 1L;
        int initialStock = 10;
        int quantityToAdd = 0;

        Book book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(bookId, quantityToAdd);

        // Assert
        assertTrue(result);
        assertThat(book.getStockQuantity(), equalTo(initialStock)); // Should remain unchanged
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(eq(bookId), any(Book.class));
    }

    @Test
    public void should_update_stock_to_exactly_zero() {
        // Arrange
        long bookId = 1L;
        int initialStock = 5;
        int quantityToRemove = -5; // Remove all stock

        Book book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(bookId, quantityToRemove);

        // Assert
        assertTrue(result);
        assertThat(book.getStockQuantity(), equalTo(0));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(eq(bookId), any(Book.class));
    }

    @Test
    public void should_return_false_when_trying_to_reduce_stock_by_one_more_than_available() {
        // Arrange
        long bookId = 1L;
        int initialStock = 5;
        int quantityToRemove = -6; // Try to remove one more than available

        Book book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(bookId, quantityToRemove);

        // Assert
        assertFalse(result);
        assertThat(book.getStockQuantity(), equalTo(initialStock)); // Stock should remain unchanged
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).update(anyLong(), any(Book.class));
    }

    @Test
    public void should_update_stock_with_large_positive_quantity() {
        // Arrange
        long bookId = 1L;
        int initialStock = 10;
        int largeQuantityToAdd = 1000;

        Book book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(bookId, largeQuantityToAdd);

        // Assert
        assertTrue(result);
        assertThat(book.getStockQuantity(), equalTo(initialStock + largeQuantityToAdd));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(eq(bookId), any(Book.class));
    }

    @Test
    public void should_update_stock_with_negative_book_id() {
        // Arrange
        long negativeBookId = -5L;
        int initialStock = 10;
        int quantityToAdd = 5;

        Book book = Book.builder()
                .id(negativeBookId)
                .title("Test Book")
                .author("Test Author")
                .price(15.0)
                .stockQuantity(initialStock)
                .build();

        when(bookRepository.findById(negativeBookId)).thenReturn(book);

        // Act
        boolean result = bookService.updateStock(negativeBookId, quantityToAdd);

        // Assert
        assertTrue(result);
        assertThat(book.getStockQuantity(), equalTo(initialStock + quantityToAdd));
        verify(bookRepository, times(1)).findById(negativeBookId);
        verify(bookRepository, times(1)).update(eq(negativeBookId), any(Book.class));
    }
}
