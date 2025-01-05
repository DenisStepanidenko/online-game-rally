package DM.ServerRally.multiplaystats.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopScoresByWinsDto {
    private String username;
    private Integer wins;
}
