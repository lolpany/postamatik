package lol.lolpany.postamatik;

import com.google.api.services.youtube.YouTube;
import lol.lolpany.Account;
import lol.lolpany.AccountsConfig;
import lol.lolpany.ComponentConnection;
import lol.lolpany.Location;
import lol.lolpany.postamatik.pornhub.PornhubLocation;
import lol.lolpany.postamatik.youtube.YoutubeApi;
import lol.lolpany.postamatik.youtube.YoutubeDesignation;
import lol.lolpany.postamatik.youtube.YoutubeLocation;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.HOURS;

public class LocationSwitcher implements ComponentCycle {

    private final ComponentConnection<AccountsConfig> accountsConfigQueue;
    private final ComponentConnection<Triple<Switch, Account, Location<LocationConfig>>> locationConfigSolverQueue;

    private AccountsConfig<LocationConfig> accountsConfig;
    private final Map<String, Switch> locationUrlToSwitch;

    public LocationSwitcher(ComponentConnection<AccountsConfig> accountsConfigQueue,
                            ComponentConnection<Triple<Switch, Account, Location<LocationConfig>>> locationConfigSolverQueue) {
        this.accountsConfigQueue = accountsConfigQueue;
        this.locationConfigSolverQueue = locationConfigSolverQueue;
        this.accountsConfig = null;
        this.locationUrlToSwitch = new HashMap<>();
    }

    @Override
    public void doCycle() throws Exception {
        AccountsConfig<LocationConfig> newAccountsConfig = accountsConfigQueue.poll();
        if (newAccountsConfig != null) {
            accountsConfig = newAccountsConfig;
        }
        if (accountsConfig != null) {
            for (Account<LocationConfig> account : accountsConfig.accountsConfig) {
                for (Location<LocationConfig> location : account.locations) {
                    if (location instanceof YoutubeLocation) {
                        YouTube youTube = YoutubeApi
                                .fetchYouTube(account, (YoutubeLocation) location,
                                        YoutubeDesignation.LOCATION_SWITCHER);

                        Switch newSwitch = youTube.channels()
                                .list("auditDetails")
                                .setMine(true)
                                .execute().getItems().get(0).getAuditDetails().getCopyrightStrikesGoodStanding() ?
                                Switch.ENABLE : Switch.DISABLE;

                        String url = location.url.toString();
                        if (locationUrlToSwitch.get(url) == null || locationUrlToSwitch.get(url) != newSwitch) {
                            locationConfigSolverQueue.offer(new ImmutableTriple<>(newSwitch, account, location));
                            locationUrlToSwitch.put(url, newSwitch);
                        }

                    } else {
                        Switch newSwitch = Switch.ENABLE;
                        locationConfigSolverQueue.offer(new ImmutableTriple<>(newSwitch, account, location));
                        locationUrlToSwitch.put(location.url.toString(), newSwitch);
                    }
                }
            }
        }
        sleep(HOURS.toMillis(1));
    }
}
