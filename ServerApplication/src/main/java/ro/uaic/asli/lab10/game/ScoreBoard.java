package ro.uaic.asli.lab10.game;

import ro.uaic.asli.lab10.model.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Renders score tables and determines the winner using lab rules:
 * highest score wins; ties break by lowest total response time.
 */
public final class ScoreBoard {

    private ScoreBoard() {
    }

    public static String renderTable(List<Player> players) {
        List<Player> copy = new ArrayList<>(players);
        copy.sort(Comparator.comparingInt(Player::getScore).reversed()
                .thenComparingLong(Player::getTotalResponseTimeMs));
        StringBuilder sb = new StringBuilder();
        sb.append("SCOREBOARD|sorted=score_desc_then_time_asc\n");
        int rank = 1;
        for (Player p : copy) {
            sb.append("ROW|rank=").append(rank++).append("|").append(p.scoreLine()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Winner rule: max score; if tie, player with minimum total response time wins.
     */
    public static Optional<Player> winner(List<Player> players) {
        if (players.isEmpty()) {
            return Optional.empty();
        }
        return players.stream().min(
                Comparator.comparingInt(Player::getScore).reversed()
                        .thenComparingLong(Player::getTotalResponseTimeMs)
        );
    }
}
