package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.matcher.control.LabeledMatchers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.robot.impl.SleepRobotImpl;
import org.testfx.util.WaitForAsyncUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import application.control.DailyBankMainFrame;
import application.control.ExceptionDialog;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.data.Employe;
import model.orm.Access_BD_Test;
import model.orm.exception.ApplicationException;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;

import static org.testfx.api.FxAssert.verifyThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestBank extends ApplicationTest {

    private static DailyBankState dailyBankState;
    
    private static boolean resestBD = false;
    private static boolean test1Passed = true;

    @BeforeAll
    public static void before() throws Exception{
        ApplicationTest.launch(DailyBankMainFrame.class);
        dailyBankState = DailyBankMainFrame.getDailyBankState();
    }

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    public void beforeEach() {
        release(new KeyCode[] {});
        release(new MouseButton[] {});
    }

    public void resestBD() {
        if(!resestBD) {
            Access_BD_Test abt = new Access_BD_Test();
            try {
                abt.resestBD();
                resestBD = true;
            } catch (DataAccessException | DatabaseConnexionException e) {
                e.printStackTrace();
                resestBD = false;
            }
        }
    }

    public void verifCoDECO() {
        Button btnDeconn = find("#btnDeconn");
        if (btnDeconn != null) {
            clickOn("#btnDeconn");
        }
    }

    public void connecter(String login, String motPasse) {
        //lancer la feneêtre de connexion
        clickOn("#btnConn");

        //remplir les champs de login
        clickOn("#txtLogin").write(login);
        clickOn("#txtPassword").write(motPasse);
        clickOn("#btnValider");
    }

    @Test
    @Order(1)
    public void testLogin() {
        resestBD();

        String login = "Tuff";
        String motPasse = "Lejeune";
        String prenom = "Michel";
        String nom = "Tuffery";

        verifCoDECO();
        connecter(login, motPasse);
        
        //verifier que les labels sont bien remplis avec les infos de l'employer
        verifyThat("#lblEmpNom", LabeledMatchers.hasText(nom));
        verifyThat("#lblEmpPrenom", LabeledMatchers.hasText(prenom));

        //verifier que l'employer dans dailyBankState est le même
        try {
            assertEquals(this.dailyBankState.getEmployeActuel().login, login);
            assertEquals(this.dailyBankState.getEmployeActuel().motPasse, motPasse);
            assertEquals(this.dailyBankState.getEmployeActuel().prenom, prenom);
            assertEquals(this.dailyBankState.getEmployeActuel().nom, nom);
        } catch (AssertionError e) {
            test1Passed = false;
        }
    }

    @Test
    public void testListEmploye() {
        resestBD();
        Assumptions.assumeTrue(test1Passed, "Test1 echec, impossible d'executer les autres tests");
        int nbEmployeBD = 0;
        int nbEmployeLV = 0;
        ArrayList<Employe> employesBD = null;
        ArrayList<Employe> employesLV = null;
        Employe employeBD = null;
        Employe employeLV = null;
        
        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        verifCoDECO();
        connecter(login, motPasse);

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
            System.exit(1);
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


    @Test
    public void tuff() {
        ArrayList<Employe> employesBD = null;

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        // verifCoDECO();
        // connecter(login, motPasse); 

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        Access_BD_Test access_BD_Test = new Access_BD_Test();
        try {
            employesBD = access_BD_Test.getAllEmploye();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        for (Employe employeVeif : employesBD) {
            while(true){
            String strNom;
            String strPrenom;

            doubleClickOn(employeVeif.toString());

            TextField txtIdEmpl = findtkt("#txtIdEmpl");
            System.out.println("id :" + txtIdEmpl.getText());
            assertEquals(employeVeif.idEmploye, Integer.parseInt(txtIdEmpl.getText()));
            
            TextField txtNom = findtkt(employeVeif.nom);
            TextField txtPrenom = findtkt(employeVeif.prenom);

            System.out.println("nom :" + txtNom.getText());
            System.out.println("prenom :" + txtPrenom.getText());
            
            assertEquals(employeVeif.nom, txtNom.getText());
            assertEquals(employeVeif.prenom, txtPrenom.getText());

            System.out.println("droit : " + employeVeif.droitsAccess);
            
            if(employeVeif.droitsAccess.equals("chefAgence")) {
                verifyThat("#menuBtnDroitAccess", LabeledMatchers.hasText("Chef d'agence"));
            } else {
                verifyThat("#menuBtnDroitAccess", LabeledMatchers.hasText("Guichetier"));
            }

            TextField txtLogin = findtkt(employeVeif.login);
            TextField txtMotPasse = findtkt(employeVeif.motPasse);

            assertEquals(employeVeif.login, txtLogin.getText());
            assertEquals(employeVeif.motPasse, txtMotPasse.getText());

            TextField txtIdAg = findtkt("#txtIdAgence");
            assertEquals(employeVeif.idAg, Integer.parseInt(txtIdAg.getText()));

            clickOn("#butOk");
        }
    }
    }

    @Test
    public void testConsulterEmploye() {
        ArrayList<Employe> employesBD = null;

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        // verifCoDECO();
        // connecter(login, motPasse); 

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        Access_BD_Test access_BD_Test = new Access_BD_Test();
        try {
            employesBD = access_BD_Test.getAllEmploye();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        for (Employe employeVeif : employesBD) {
            String strNom;
            String strPrenom;

            doubleClickOn(employeVeif.toString());

            TextField txtIdEmpl = find("#txtIdEmpl");
            System.out.println("id :" + txtIdEmpl.getText());
            assertEquals(employeVeif.idEmploye, Integer.parseInt(txtIdEmpl.getText()));
            
            TextField txtNom = find(employeVeif.nom);
            TextField txtPrenom = find(employeVeif.prenom);

            System.out.println("nom :" + txtNom.getText());
            System.out.println("prenom :" + txtPrenom.getText());
            
            assertEquals(employeVeif.nom, txtNom.getText());
            assertEquals(employeVeif.prenom, txtPrenom.getText());

            System.out.println("droit : " + employeVeif.droitsAccess);
            
            if(employeVeif.droitsAccess.equals("chefAgence")) {
                verifyThat("#menuBtnDroitAccess", LabeledMatchers.hasText("Chef d'agence"));
            } else {
                verifyThat("#menuBtnDroitAccess", LabeledMatchers.hasText("Guichetier"));
            }

            TextField txtLogin = find(employeVeif.login);
            TextField txtMotPasse = find(employeVeif.motPasse);

            assertEquals(employeVeif.login, txtLogin.getText());
            assertEquals(employeVeif.motPasse, txtMotPasse.getText());

            TextField txtIdAg = find("#txtIdAgence");
            assertEquals(employeVeif.idAg, Integer.parseInt(txtIdAg.getText()));

            clickOn("#butOk");
        }
    }


    /**
     * Méthode pour trouver un élément dans la fenêtre de test à partir de son id
     * @param <T>
     * @param query
     * @return
     */
    public <T extends Node> T find(final String query) { 
        Set<Node> nodes = lookup(query).queryAll();
        if (nodes.isEmpty()) {
            return null;
        }
        return (T) nodes.iterator().next();
    } 
    
    public <T extends Node> T findtkt(final String query) { 
        Set<Node> nodes = lookup(query).queryAll();
        if (nodes.isEmpty()) {
            return null;
        }
        for (Node node : nodes) {
            System.err.println(getStageFromNode(node));
            if (getStageFromNode(node) != null) {
                if (!getStageFromNode(node).isFocused()) {
                    System.out.println("remove" + node + " from " + getStageFromNode(node) );
                    //nodes.remove(node);
                    System.out.println("size" + nodes.size());
                    for (Node nodee : nodes) {
                        System.out.println(nodee);
                    }
                } else {
                    System.err.println("pas de modality");
                }
            }
        }
        return (T) nodes.iterator().next();
    } 

    public Stage getStageFromNode(Node node) {
        Scene scene = node.getScene();
        if (scene != null) {
            Window window = scene.getWindow();
            if (window instanceof Stage) {
                return (Stage) window;
            }
        }
        return null;
    }
}
