package DM.ServerRally.multiplaystats.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopScoresByTimeDto {
    private String username;

    @JsonProperty("best_time")
    private Double bestTime;
}
