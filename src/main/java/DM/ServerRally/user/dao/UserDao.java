package DM.ServerRally.user.dao;

import DM.ServerRally.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_SAVE_USER = "INSERT INTO users (username, password) VALUES (?,?)";
    private static final String SQL_FIND_USER_BY_ID = "SELECT * FROM users WHERE username = ?";

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveUser(String username, String password) {
        jdbcTemplate.update(SQL_SAVE_USER, username, password);
    }

    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query(SQL_FIND_USER_BY_ID, rs -> rs.next() ? Optional.of(User.builder()
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .build()) : Optional.empty(), username);
    }
}
