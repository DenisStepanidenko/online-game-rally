package DM.ServerRally.user.service;

import DM.ServerRally.user.dao.UserDao;
import DM.ServerRally.user.model.User;
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

    public void saveUser(String username, String password) {
        userDao.saveUser(username, password);
    }

    public Optional<User> findUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

}
