package application;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import application.control.DailyBankMainFrame;
import application.control.ExceptionDialog;
import javafx.stage.Stage;
import model.data.Employe;
import model.orm.Access_BD_Employe;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

import static org.testfx.api.FxAssert.verifyThat;

public class LoginTest extends ApplicationTest {

    private DailyBankState dailyBankState;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        new DailyBankMainFrame().start(stage);
    }

    @Test
    public void testLogin() {
        clickOn("#btnConn");

        clickOn("#txtLogin").write("Tuff");
        clickOn("#txtPassword").write("Lejeune");
        clickOn("#btnValider");
    }
}
