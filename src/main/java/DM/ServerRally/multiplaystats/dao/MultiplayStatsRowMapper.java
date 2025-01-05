package DM.ServerRally.multiplaystats.dao;

import DM.ServerRally.multiplaystats.model.MultiplayStats;
import DM.ServerRally.user.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MultiplayStatsRowMapper implements RowMapper<MultiplayStats> {

    @Override
    public MultiplayStats mapRow(ResultSet rs, int rowNum) throws SQLException {
        MultiplayStats stats = new MultiplayStats();
        stats.setId(rs.getInt("stats_id"));
        stats.setWins(rs.getInt("wins"));
        stats.setBestTime(rs.getDouble("best_time"));


        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));


        stats.setUser(user);

        return stats;
    }
}
