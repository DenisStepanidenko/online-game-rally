package DM.ServerRally.multiplaystats.service;


import DM.ServerRally.multiplaystats.dao.MultiplayStatsDao;
import DM.ServerRally.multiplaystats.dto.TopScoresByTimeDto;
import DM.ServerRally.multiplaystats.dto.TopScoresByWinsDto;
import DM.ServerRally.multiplaystats.model.MultiplayStats;
import DM.ServerRally.user.dao.UserJpa;
import DM.ServerRally.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MultiplayStatsService {
    private final MultiplayStatsDao multiplayStatsDao;
    private final UserJpa userJpa;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public MultiplayStatsService(MultiplayStatsDao multiplayStatsDao, UserJpa userJpa) {
        this.multiplayStatsDao = multiplayStatsDao;
        this.userJpa = userJpa;
    }

    @Transactional
    public String getTopScoresByWinsJson() {
        List<MultiplayStats> multiplayStatsList = multiplayStatsDao.getTopScoreByWins();

        List<TopScoresByWinsDto> topScoresDtos = new ArrayList<>();

        for (MultiplayStats stats : multiplayStatsList) {
            TopScoresByWinsDto currentDto = new TopScoresByWinsDto();
            currentDto.setUsername(stats.getUser().getUsername());
            currentDto.setWins(stats.getWins());
            topScoresDtos.add(currentDto);
        }

        try {
            return objectMapper.writeValueAsString(topScoresDtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String getTopScoresByTimeJson() {
        List<MultiplayStats> multiplayStatsList = multiplayStatsDao.getTopScoresByTime();


        List<TopScoresByTimeDto> topScoresDtos = new ArrayList<>();

        for (MultiplayStats stats : multiplayStatsList) {
            TopScoresByTimeDto currentDto = new TopScoresByTimeDto();
            currentDto.setUsername(stats.getUser().getUsername());
            currentDto.setBestTime(stats.getBestTime());
            topScoresDtos.add(currentDto);
        }

        try {
            return objectMapper.writeValueAsString(topScoresDtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void updateTime(String username, double finishTime) {
        User user = userJpa.findByUsername(username).get();

        MultiplayStats stat = user.getMultiplayStats();

        if (Objects.isNull(stat)) {
            stat = new MultiplayStats();
            stat.setUser(user);
            stat.setWins(0);
            stat.setBestTime(finishTime);
            user.setMultiplayStats(stat);
        } else {
            if (stat.getBestTime() > finishTime) {
                stat.setBestTime(finishTime);
            }
        }

        userJpa.save(user);
    }

    @Transactional
    public void updateWins(String username) {
        User user = userJpa.findByUsername(username).get();

        MultiplayStats stat = user.getMultiplayStats();
        stat.setWins(stat.getWins() + 1);

        userJpa.save(user);
    }
}
