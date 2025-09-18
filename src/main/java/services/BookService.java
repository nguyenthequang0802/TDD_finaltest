package services;

import models.Book;
import org.springframework.stereotype.Service;
import repositories.IBookRepository;

import java.util.List;

@Service
public class BookService {
    private final IBookRepository bookRepository;

    public BookService(IBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book getBookById(long id) {
        Book book = bookRepository.findById(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with id: " + id);
        }
        return book;
    }

    public Book createBook(Book book) {
        if (book.getTitle() == null || book.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (book.getAuthor() == null || book.getAuthor().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be empty");
        }
        if (book.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        return bookRepository.create(book);
    }

    public Book updateBook(long id, Book book) {
        Book existingBook = bookRepository.findById(id);
        if (existingBook == null) {
            throw new IllegalArgumentException("Book not found with id: " + id);
        }
        return bookRepository.update(id, book);
    }

    public void deleteBook(long id) {
        Book existingBook = bookRepository.findById(id);
        if (existingBook == null) {
            throw new IllegalArgumentException("Book not found with id: " + id);
        }
        bookRepository.delete(id);
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }
        return bookRepository.searchByKeyword(keyword);
    }

    public boolean updateStock(long bookId, int quantity) {
        Book book = bookRepository.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }

        if (book.getStockQuantity() + quantity < 0) {
            return false; // Không đủ số lượng trong kho
        }

        book.setStockQuantity(book.getStockQuantity() + quantity);
        bookRepository.update(bookId, book);
        return true;
    }
}
