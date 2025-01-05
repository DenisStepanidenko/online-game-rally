package DM.ServerRally.user.dao;

import DM.ServerRally.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpa extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
