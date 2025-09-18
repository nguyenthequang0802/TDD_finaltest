package services;

import models.User;
import org.springframework.stereotype.Service;
import repositories.IUserRepository;

@Service
public class UserService {
    IUserRepository repository;

    public UserService(IUserRepository repository){
        this.repository = repository;
    }

    public User createUser(User data){
        if(this.repository.findByEmail(data.getEmail()) != null){
            throw new IllegalArgumentException("Email already exists");
        }
        return this.repository.create(data);
    }

    public long login(String email, String password){
        User user = this.repository.findByEmail(email);

        if(user == null){
            throw new IllegalArgumentException("User not found");
        }
        if(!user.getPassword().equals(password)){
            throw new IllegalArgumentException("Wrong password");
        }
        return user.getId();
    }
}
