package services;

import models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.IUserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    IUserRepository repository;

    @InjectMocks
    UserService service;

    private final User userDefault = new User(
            1L, "quang", "123456", "user@gmail.com", "Nguyen The Quang", "Ha Noi", "0927266363"
    );

    @Test
    public void should_return_registered_user_with_email_not_in_used() {
        when(repository.findByEmail("user@gmail.com")).thenReturn(null);
        when(repository.create(any(User.class))).thenReturn(userDefault);

        User user = service.createUser(userDefault);

        assertThat(user, notNullValue());
        assertThat(user.getId(), equalTo(1L));
        verify(repository, times(1)).create(any(User.class));
    }

    @Test
    public void should_return_registered_user_with_email_is_in_used() {
        User exitingUser = User.builder().id(1L).email("user@gmail.com").build();

        when(repository.findByEmail("user@gmail.com")).thenReturn(exitingUser);

        assertThrows(IllegalArgumentException.class, () -> service.createUser(exitingUser));
        verify(repository, times(0)).create(any(User.class));
    }

    @Test
    public void should_return_registered_userId_with_email_exist() {
        when(repository.findByEmail("user@gmail.com")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.login("user@gmail.com", "123456"));
        assertThat(exception.getMessage(), equalTo("User not found"));
    }

    @ParameterizedTest
    @CsvSource({
            "user@gmail.com, 654321",
            "user@gmail.com, "
    })
    public void should_return_error_with_password_not_matching_password(String email, String password) {
        when(repository.findByEmail(email)).thenReturn(this.userDefault);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.login(email, password));

        assertThat(exception.getMessage(), equalTo("Wrong password"));
    }

    @ParameterizedTest
    @CsvSource({
            "user@gmail.com, 123456"
    })
    public void should_return_userId_when_checking_exiting_user(String email, String password) {
        when(repository.findByEmail(email)).thenReturn(this.userDefault);

        long userId = service.login(email, password);
        assertThat(userId, equalTo(1L));
        verify(repository, times(1)).findByEmail(email);
    }
}
