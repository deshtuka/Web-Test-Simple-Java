package appline.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class ScenarioAppline {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void before() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/webdriver/chromedriver.exe");
        driver = new ChromeDriver();

        // Настройка: полный экран + ожидания
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 10, 1000);

        driver.get("http://training.appline.ru/user/login");
    }

    @Test
    public void testScenario() {
        // Шаг 1. Авторизация
        WebElement checkAuthPage = driver.findElement(By.xpath("//form[@id='login-form']"));
        Assert.assertTrue("Страница 'Авторизации' - не открыта", checkAuthPage.isDisplayed());

        driver.findElement(By.xpath("//*[@name='_username']")).sendKeys("Irina Filippova");
        driver.findElement(By.xpath("//*[@name='_password']")).sendKeys("testing");
        driver.findElement(By.id("_submit")).click();

        // Шаг 2. Проверить наличие на странице заголовка "Панель быстрого запуска"
        WebElement checkHeaderBar = driver.findElement(By.xpath("//h1[@class='oro-subtitle']"));
        Assert.assertTrue(
                "Заголовок 'Панель быстрого запуска' - отсутствует на странице",
                checkHeaderBar.isDisplayed() && checkHeaderBar.getText().contains("Панель быстрого запуска")
        );

        // Шаг 3. В выплывающем окне раздела "Расходы" нажать на "Командировки"
        WebElement btnCosts = driver.findElement(By.xpath("//*[@id='main-menu']/ul/li/a/span[text()='Расходы']"));
        btnCosts.click();

        WebElement btnTrip = driver.findElement(By.xpath("//li[@data-route='crm_business_trip_index']//span"));
        Assert.assertTrue("Выпадающий список раздела 'Расходы' - не открылся", btnTrip.isDisplayed());
        btnTrip.click();

        // Окно загрузки
        loading();

        // Шаг 4. Нажать на "Создать командировку"
        WebElement btnCreateTrip = driver.findElement(By.xpath("//div[@class='btn-group']//a[@href='/business-trip/create/']"));
        Assert.assertTrue("Кнопка 'Создать командировку' - отсутствует", btnCreateTrip.isDisplayed());
        btnCreateTrip.click();

        // Окно загрузки
        loading();

        // Шаг 5. Проверить наличие на странице заголовка "Создать командировку"
        WebElement headCreateTrip = driver.findElement(By.xpath("//h1[@class='user-name']"));

        Assert.assertTrue("Страница 'Создания командировки' - не открылась", headCreateTrip.isDisplayed());

        String errorMessage = "Текст заголовка страницы не совпал!";
        Assert.assertEquals(errorMessage, "Создать командировку", headCreateTrip.getText());

        // Шаг 6. На странице создания командировки заполнить или выбрать поля + проверить правильность заполнения
        //— Подразделение - выбрать "Отдел внутренней разработки"
        WebElement selectSubdivision = driver.findElement(By.xpath("//*[@data-ftid='crm_business_trip_businessUnit']"));
        Select dropdown = new Select(selectSubdivision);
        dropdown.selectByVisibleText("Отдел внутренней разработки");

        //— Принимающая организация - нажать "Открыть список" и в поле "Укажите организацию" выбрать любое значение
        WebElement inputHostOrganization = driver.findElement(By.xpath("//*[@data-ftid='crm_business_trip_company']"));
        String valueBeforeHostOrganization = inputHostOrganization.getAttribute("value");

        driver.findElement(By.xpath("//*[@id='company-selector-show']")).click();

        WebElement checkCompanyContainer = driver.findElement(By.xpath("//*[@class='company-container']"));
        Assert.assertTrue("Раздел 'Укажите организацию' - не появился", checkCompanyContainer.isDisplayed());

        checkCompanyContainer.findElement(By.xpath(".//a")).click();
        WebElement optionRandom = driver.findElement(By.xpath("//*[@id='select2-drop']//li[2]"));
        Assert.assertTrue("Выпадающий список 'Организаций' - не открылся", optionRandom.isDisplayed());
        optionRandom.click();

        String valueAfterHostOrganization = inputHostOrganization.getAttribute("value");
        Assert.assertNotEquals("Значение поля 'Принимающая организация' - не изменилось", valueBeforeHostOrganization, valueAfterHostOrganization);

        //— В задачах поставить чекбокс на "Заказ билетов"
        WebElement checkboxTripTasks = driver.findElement(By.xpath("//*[@data-ftid='crm_business_trip_tasks']//*[text()='Заказ билетов']/../input"));
        checkboxTripTasks.click();
        Assert.assertTrue("Чекбокс 'Заказ билетов' - не проставлен", checkboxTripTasks.isSelected());

        //— Указать города выбытия и прибытия
        enterValueIntoInputAndCheck(driver, "Город выбытия", "//div/input[contains(@id, 'departureCity')]", "Россия, Санкт-Петербург");
        enterValueIntoInputAndCheck(driver, "Город прибытия", "//div/input[contains(@id, 'arrivalCity')]", "Россия, Владивосток");

        //—Указать даты выезда и возвращения
        enterValueIntoInputAndCheck(driver, "Дата выезда", "//div/input[contains(@id, 'departureDatePlan')]", "01.01.2022");
        enterValueIntoInputAndCheck(driver, "Дата возвращения", "//div/input[contains(@id, 'returnDatePlan')]", "02.02.2022");

        // Шаг 7. Нажать "Сохранить и закрыть"
        driver.findElement(By.xpath("//body")).click();
        driver.findElement(By.xpath("//button[contains(@data-action, 'business_trip_index')]")).click();

        // Окно загрузки
        loading();

        // Шаг 8. Проверить, что на странице появилось сообщение: "Список командируемых сотрудников не может быть пустым"
        List<WebElement> textFaileds = driver.findElements(By.xpath("//*[@class='validation-failed']"));
        for (WebElement textFailed : textFaileds) {
            String expectedMessage = "Список командируемых сотрудников не может быть пустым";
            Assert.assertEquals("Не корректный текст ошибки", expectedMessage, textFailed.getAttribute("innerText"));
        }
    }

    @After
    public void after(){
        driver.quit();
    }

    public void enterValueIntoInputAndCheck(WebDriver driver, String fieldName, String xpath, String value) {
        // Заполнение полей типа Input + проверка
        if (driver != null) {
            WebElement inputValue = driver.findElement(By.xpath(xpath));
            inputValue.clear();
            inputValue.sendKeys(value);
            String errorMessage = fieldName + " заполнен(-а) не верно!";
            Assert.assertEquals(errorMessage, value, inputValue.getAttribute("value"));
        }
    }

    public void loading() {
        // Ожидание появления и исчезновения окна "Загрузка..."
        WebElement msgLoader = driver.findElement(By.xpath("//div[@class='loader-mask shown']"));
        wait.until(visibilityOf(msgLoader));
        wait.until(invisibilityOf(msgLoader));
    }

}
