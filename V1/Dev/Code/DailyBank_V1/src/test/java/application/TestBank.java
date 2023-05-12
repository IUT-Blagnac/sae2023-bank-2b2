package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
// import static org.testfx.matcher.control.TextInputControlMatchers.hasText;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import application.control.DailyBankMainFrame;
import application.control.ExceptionDialog;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.data.Employe;
import model.orm.Access_BD_Employe;
import model.orm.Access_BD_Test;
import model.orm.exception.ApplicationException;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;

import static org.testfx.api.FxAssert.verifyThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestBank extends ApplicationTest {

    private DailyBankState dailyBankState;

    @BeforeAll
    public static void setUpLogin() throws Exception {
    }

    @Override
    public void start(Stage stage) {
        new DailyBankMainFrame().start(stage);
        this.dailyBankState = DailyBankMainFrame.getDailyBankState();
    }

    @Test
    @Order(1)
    public void testLogin() {
        String login = "Tuff";
        String motPasse = "Lejeune";
        String prenom = "Michel";
        String nom = "Tuffery";

        //lancer la feneêtre de connexion
        clickOn("#btnConn");

        //remplir les champs de login
        clickOn("#txtLogin").write(login);
        clickOn("#txtPassword").write(motPasse);
        clickOn("#btnValider");
        
        //verifier que les labels sont bien remplis avec les infos de l'employer
        verifyThat("#lblEmpNom", hasText(nom));
        verifyThat("#lblEmpPrenom", hasText(prenom));

        //verifier que l'employer dans dailyBankState est le même
        assertEquals(this.dailyBankState.getEmployeActuel().login, login);
        assertEquals(this.dailyBankState.getEmployeActuel().motPasse, motPasse);
        assertEquals(this.dailyBankState.getEmployeActuel().prenom, prenom);
        assertEquals(this.dailyBankState.getEmployeActuel().nom, nom);
    }

    @Test
    public void testListEmploye() {
        int nbEmployeBD = 0;
        int nbEmployeLV = 0;
        ArrayList<Employe> employesBD = null;
        ArrayList<Employe> employesLV = null;
        Employe employeBD = null;
        Employe employeLV = null;
        
        //login
        String login = "Tuff";
        String motPasse = "Lejeune";
        clickOn("#btnConn");
        clickOn("#txtLogin").write(login);
        clickOn("#txtPassword").write(motPasse);
        clickOn("#btnValider");

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        //récupérer le nombre d'employe dans la liste view affcihée
        ListView lvEmployes = find("#lvEmployes");
        nbEmployeLV = lvEmployes.getItems().size();

        //récupérer le nombre d'employe dans la BD
        Access_BD_Test access_BD_Test = new Access_BD_Test();
        try {
            nbEmployeBD = access_BD_Test.getNumberEmploye();
        } catch (DataAccessException | DatabaseConnexionException e) {
            e.printStackTrace();
        }

        //verifier que le nombre d'employe dans la BD est le même que dans la liste view
        assertEquals(nbEmployeLV, nbEmployeBD);

        
        try {
            employesBD = access_BD_Test.getAllEmploye();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }
        employesLV = new ArrayList<>(lvEmployes.getItems());

       //foreach dans deux ArrayList à la fois  pour vérifier que les employes sont les mêmes
        for(int i = 0; i < employesBD.size() && i < employesLV.size(); i++) {
            employeBD = employesBD.get(i);
            employeLV = employesLV.get(i);
            assertEquals(employeBD.toString(), employeLV.toString());
        }
    }


    /**
     * Méthode pour trouver un élément dans la fenêtre de test à partir de son id
     * @param <T>
     * @param query
     * @return
     */
    public <T extends Node> T find(final String query) { 
        return (T) lookup(query).queryAll().iterator().next();
    }   
}
