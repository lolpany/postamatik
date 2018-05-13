package lol.lolpany.friendify.vk;

import com.codeborne.selenide.Configuration;
import org.junit.Test;

import java.io.FileNotFoundException;

import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;

public class Vk {

    private static final int PEOPLE_ADDITION_BATCH_SIZE = 2;
    private static final int PEOPLE_ADDITION_TIMEOUT = 5000;
    private static final int PEOPLE_BATCH_ADDITION_TIMEOUT = 200000;

    @Test
    public void go() throws FileNotFoundException {

        Configuration.timeout = 10000;
//
//        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
//        ChromeOptions chromeOpts = new ChromeOptions();
////        chromeOpts.addArguments("headless");
//        WebDriver driver  = new ChromeDriver(chromeOpts);
//        setWebDriver(driver);
//
//        Gson gson = new GsonBuilder()
//                .registerTypeAdapter(PostAction.class, new PostActionDeserializer())
//                .registerTypeAdapter(Location.class, new LocationDeserializer())
//                .registerTypeAdapter(ContentSearch.class, new ContentSearchDeserializer())
//                .setPrettyPrinting()
//                .create();
//
//        lol.lolpany.friendify.AccountsConfig accountsConfig = gson.fromJson(
//                new FileReader("D:\\storage\\info\\buffer\\postamatik\\accounts-config\\accounts-config.json"),
//                lol.lolpany.friendify.AccountsConfig.class);
//
//        authorize(accountsConfig.accountsConfig.get(accountsConfig.accountsConfig.size() - 1));
//
//
//        open("https://vk.com/friends?act=find&c%5Bcompany%5D=intel&c%5Bcountry%5D=9&c%5Bper_page%5D=40&c%5Bphoto%5D=1&c%5Bsection%5D=people");
//
//
//        int i = 0;
//        Random random = new Random();
//        while ($$("div.people_row").get(i).is(Condition.exist)) {
//            $$("div.people_row").get(max(i - 1, 0)).scrollTo().should(Condition.visible);
//            SelenideElement button = $$("div.people_row").get(i).find("div.controls button");
//            if (button.getText().equals("Add to friends")) {
//                button.click();
//                sleep(round(random.nextDouble() * PEOPLE_ADDITION_TIMEOUT));
//                if ($("div.recaptcha").find("iframe").exists()) {
//                    driver.switchTo().frame($("div.recaptcha").find("iframe"));
//                    $("div.recaptcha-checkbox-checkmark").click();
//                }
//            }
//            if (i % PEOPLE_ADDITION_BATCH_SIZE == 0) {
//                sleep(PEOPLE_BATCH_ADDITION_TIMEOUT);
//            }
//            i++;
//        }


        closeWebDriver();
    }

//    private void authorize(Account account) {
//        open("https://vk.com");
//        $("input#index_email").sendKeys(account.login);
//        $("input#index_pass").sendKeys(account.password);
//        $("button#index_login_button").click();
//        $("button#index_login_button").should(Condition.not(Condition.visible));
//    }


}
