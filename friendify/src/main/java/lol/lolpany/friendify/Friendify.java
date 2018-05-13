package lol.lolpany.friendify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.lolpany.*;
import lol.lolpany.friendify.linkedin.LinkedInConnectorFactory;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Friendify {

    public static final String CONFIG_DIR = "D:\\storage\\info\\buffer\\friendify\\accounts-config";
    public static final String FILE_PATH = CONFIG_DIR + "\\accounts-config.json";

    private static Map<String, ConnectorFactory> connectorFactoryByUrl = new HashMap<String, ConnectorFactory>(){{
        put("www.linkedin.com", new LinkedInConnectorFactory());
    }};

    //    @Test
    public static void main(String[] args) throws Exception {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .setPrettyPrinting()
                .create();

        AtomicBoolean isOn = new AtomicBoolean(true);


        AccountsConfig<LocationConfig> accountsConfig = gson.fromJson(
                new FileReader(FILE_PATH),
                AccountsConfig.class);

        int numberOfRunnables = accountsConfig.accountsConfig.size() * 2;

        ExecutorService executorService = new ThreadPoolExecutor(numberOfRunnables, numberOfRunnables, 5,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(numberOfRunnables));

        ComponentConnection<AccountsConfig> accountsConfigsQueue = new ComponentConnection<>(1);

        executorService.execute(new JsonConfigWatcher<>(AccountsConfig.class,
                CONFIG_DIR,
                accountsConfigsQueue, "accounts-config.json", gson, isOn));

        List<Future<Void>> futures = new ArrayList<>();


        for (Account<LocationConfig> account : accountsConfig.accountsConfig) {
            for (Location<LocationConfig> location : account.locations) {
                futures.add(executorService.submit(connectorFactoryByUrl.get(location.url.getHost()).create(isOn, account, location)));
            }
        }

        int a = 0;
        while (a != 99) {
            a = System.in.read();
        }
        isOn.set(false);
        executorService.shutdown();


    }


}
