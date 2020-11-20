package net.notfab.lindsey.core.framework.models;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlackjackModel {

    @Setter
    @Getter
    private long id;

    @Setter
    @Getter
    private int price;

    private final Random random = new Random();
    private int dealer;
    private int player;
    private int position;

    private final String[] cardsDealer = new String[5];
    private final String[] cardsUser = new String[5];

    public void start() {
        for (int i = 0; i < 2; i++) {
            int a = random.nextInt(13) + 1;
            dealer = dealer + getValue(a);
            cardsDealer[i] = getCard(a);
            int b = random.nextInt(13) + 1;
            player = player + getValue(b);
            cardsUser[i] = getCard(b);
            position = i;
        }
    }

    public boolean next() {
        int b = random.nextInt(13) + 1;
        player = player + getValue(b);
        cardsUser[position + 1] = getCard(b);
        if (player > 20) {
            List<String> lst = Arrays.asList(cardsUser);
            if (lst.contains("A(11)") && player != 21) {
                cardsUser[lst.indexOf("A(11)")] = "A(1)";
                player = player - 10;
                return true;
            }
            return false;
        }
        if (cardsUser[4] != null) {
            end();
            return false;
        }
        position = position + 1;
        return true;
    }

    public void end() {
        List<String> lst = Arrays.asList(cardsDealer);
        for (int i = 2; i < 5; i++) {
            if (lst.contains("A(11)") && dealer > 21) {
                cardsDealer[lst.indexOf("A(11)")] = "A(1)";
                dealer = dealer - 10;
            }
            if (dealer > 17) {
                break;
            }
            int a = random.nextInt(13) + 1;
            dealer = dealer + getValue(a);
            cardsDealer[i] = getCard(a);
        }
    }

    public Result getResult() {
        if (dealer < player && player < 22 || dealer > 21 && player < 22) {
            return Result.win;
        }
        if (dealer > player || player > 21) {
            return Result.lost;
        }
        return Result.draw;
    }

    public String getCard(int i) {
        return switch (i) {
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "k";
            case 14, 1 -> "A(11)";
            default -> String.valueOf(i);
        };
    }

    public Integer getValue(int i) {
        return switch (i) {
            case 11, 12, 13 -> 10;
            case 14, 1 -> 11;
            default -> i;
        };
    }

    public enum Result {
        win, lost, draw
    }

    public String getMessage(Member member) {
        return "Lindsey :" +
            "\n" + Arrays.toString(this.cardsDealer).replace("null", "?") +
            "\n\n" + member.getEffectiveName() +
            "\n" + Arrays.toString(this.cardsUser).replace("null", "?");
    }
}
