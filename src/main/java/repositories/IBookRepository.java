package repositories;

import models.Book;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBookRepository extends IBaseRepository<Book> {
    List<Book> searchByKeyword(String keyword);

//    Book findById(long id);
}
