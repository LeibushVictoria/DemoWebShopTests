package cloud.autotests.tests;

import cloud.autotests.config.App;
import com.codeborne.selenide.Configuration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Cookie;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class DemoWebShopTests {

    @BeforeAll
    static void configureBaseUrl() {
        RestAssured.baseURI = App.config.apiUrl();
        Configuration.baseUrl = App.config.webUrl();
    }

    String authCookie;
    @BeforeEach
    public void beforeEach() {
        authCookie = given()
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
    }

    @Test
    @Tag("demowebshop")
    @DisplayName("Add product to cart")
    void addToCartTest() {
        step("Add product to cart", () -> {
            given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .cookie("NOPCOMMERCE.AUTH", authCookie)
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
    void wishlistTest() {
        step("Add product to wishlist", () -> {
            given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .body("addtocart_14.EnteredQuantity=1")
                    .cookie("NOPCOMMERCE.AUTH", authCookie)
                    .when()
                    .post("/addproducttocart/details/14/2")
                    .then()
                    .statusCode(200)
                    .body("success", is(true))
                    .body("message", is("The product has been added to your <a href=\"/wishlist\">wishlist</a>"));
        });

        step("Check wishlist page", () -> {
            open("/wishlist");
            $(".wishlist-content").shouldHave(text("Black & White Diamond Heart"));
        });
    }
}
