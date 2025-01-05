package DM.ServerRally.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GameState {
    @JsonProperty("game_field")
    private int[][] gameField;

}
