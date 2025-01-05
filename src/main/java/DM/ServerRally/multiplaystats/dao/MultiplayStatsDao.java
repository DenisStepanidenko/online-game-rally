package DM.ServerRally.multiplaystats.dao;

import DM.ServerRally.multiplaystats.model.MultiplayStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MultiplayStatsDao {
    private final JdbcTemplate jdbcTemplate;


    private final String QUERY_TOP_50_BY_WINS = """
            SELECT ms.id AS stats_id, ms.wins, ms.best_time, u.id AS user_id, u.username, u.password
            FROM multiplay_stats ms
            JOIN users u ON ms.user_id = u.id
            ORDER BY ms.wins DESC
            LIMIT 50   
            """;

    private final String QUERY_TOP_50_BY_TIME = """
            SELECT ms.id AS stats_id, ms.wins, ms.best_time, u.id AS user_id, u.username, u.password
            FROM multiplay_stats ms
            JOIN users u ON ms.user_id = u.id
            ORDER BY ms.best_time ASC 
            LIMIT 50
            """;

    @Autowired
    public MultiplayStatsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MultiplayStats> getTopScoreByWins() {
        return jdbcTemplate.query(QUERY_TOP_50_BY_WINS, new MultiplayStatsRowMapper());
    }

    public List<MultiplayStats> getTopScoresByTime() {
        return jdbcTemplate.query(QUERY_TOP_50_BY_TIME, new MultiplayStatsRowMapper());
    }

}
