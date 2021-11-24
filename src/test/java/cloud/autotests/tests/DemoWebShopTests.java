package cloud.autotests.tests;

import cloud.autotests.config.demowebshop.App;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Cookie;

import static cloud.autotests.filters.CustomLogFilter.customLogFilter;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.is;

public class DemoWebShopTests extends TestBase{

    String authCookie = "";
    @BeforeEach
    public void getAndSetCookie() {
        step("Get cookie by api and set it to browser", () -> {
            authCookie = given()
                    .filter(customLogFilter().withCustomTemplates())
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .formParam("Email", App.config.userLogin())
                    .formParam("Password", App.config.userPassword())
                    .when()
                    .post("/login")
                    .then()
                    .statusCode(302)
                    .extract()
                    .cookie("NOPCOMMERCE.AUTH");

            open("/Themes/DefaultClean/Content/images/logo.png");

            getWebDriver().manage().addCookie(
                    new Cookie("NOPCOMMERCE.AUTH", authCookie));
        });
    }

    @Test
    @Tag("demowebshop")
    @DisplayName("Add product to cart")
    void addToCartTest() {
        step("Add product to cart", () -> {
            given()
                    .filter(customLogFilter().withCustomTemplates())
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .cookie("NOPCOMMERCE.AUTH")
                    .when()
                    .post("/addproducttocart/catalog/45/1/1")
                    .then()
                    .statusCode(200)
                    .body("success", is(true))
                    .body("message", is("The product has been added to your <a href=\"/cart\">shopping cart</a>"));
        });

        step("Check cart page", () -> {
            open("/cart");
            $(".page-body").shouldHave(text("Fiction"));
        });
    }

    @Test
    @Tag("demowebshop")
    @DisplayName("Add product to wishlist")
    void addToWishlistTest() {
        step("Add product to wishlist", () -> {
            given()
                    .filter(customLogFilter().withCustomTemplates())
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .body("addtocart_14.EnteredQuantity=1")
                    .cookie("NOPCOMMERCE.AUTH", authCookie)
                    .when()
                    .post("/addproducttocart/details/14/2")
                    .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath("shemas/AddToWishlistSheme.json"));
        });

        step("Check wishlist page", () -> {
            open("/wishlist");
            $(".wishlist-content").shouldHave(text("Black & White Diamond Heart"));
        });
    }
}