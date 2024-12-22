package DM.ServerRally.user.service;

import DM.ServerRally.user.dao.UserDao;
import DM.ServerRally.user.model.User;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    public void saveUser(String username, String password) {
        userDao.saveUser(username, password);
    }

    @Transactional
    public Optional<User> findUserByUsername(String username) {

        return userDao.findByUsername(username);
    }

}
