package team.swiper.location_bot.telegram;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.toggle.AbilityToggle;

import java.util.Arrays;
import java.util.List;

public class DisabledAbilities implements AbilityToggle {

    private final List<String> disabledAbilities = Arrays.asList(
            "report",
            "recover",
            "promote",
            "demote",
            "unban",
            "claim",
            "commands",
            "backup",
            "stats",
            "ban"
    );

    @Override
    public boolean isOff(Ability ab) {
        return disabledAbilities.contains(ab.name());
    }

    @Override
    public Ability processAbility(Ability ab) {
        return ab;
    }

}
