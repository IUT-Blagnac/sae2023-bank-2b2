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
    public void testModifierUnEmploye() {
        resestBD();
        Assumptions.assumeTrue(test1Passed, "Test1 echec, impossible d'executer les autres tests");
        Employe employesBD = null;

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        verifCoDECO();
        connecter(login, motPasse); 

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        Access_BD_Test access_BD_Test = new Access_BD_Test();
        try {
            employesBD = access_BD_Test.getEmploye("LN", "Levieux");
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }
        
        doubleClickOn(employesBD.toString());

        
        TextField txtNom = find("#txtNom");
        String nomVrf = txtNom.getText() + "test";
        txtNom.setText(nomVrf);

        TextField txtPrenom = find("#txtPrenom");
        String prenomVrf = txtPrenom.getText() + "test";
        txtPrenom.setText(prenomVrf);

        clickOn("Guichetier");
        clickOn("Chef d'agence");

        TextField txtLogin = find("#txtLogin");
        String loginVrf = txtLogin.getText() + "test";
        txtLogin.setText(loginVrf);

        TextField txtMotPasse = find("#txtMotPasse");
        String motPasseVrf = txtMotPasse.getText() + "test";
        txtMotPasse.setText(motPasseVrf);

        clickOn("Modifier");

        try {
            employesBD = access_BD_Test.getEmploye(loginVrf, motPasseVrf);
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        doubleClickOn(employesBD.toString());

        txtNom = find("#txtNom");
        assertEquals(nomVrf, txtNom.getText());

        txtPrenom = find("#txtPrenom");
        assertEquals(prenomVrf, txtPrenom.getText());

        MenuButton mbtnDroit = find("#menuBtnDroitAccess");
        assertEquals("Chef d'agence", mbtnDroit.getText());

        txtLogin = find("#txtLogin");
        assertEquals(loginVrf, txtLogin.getText());

        txtMotPasse = find("#txtMotPasse");
        assertEquals(motPasseVrf, txtMotPasse.getText());
    }

    @Test
    public void testConsulterEmployes() {
        resestBD();
        Assumptions.assumeTrue(test1Passed, "Test1 echec, impossible d'executer les autres tests");

        ArrayList<Employe> employesBD = null;
        Access_BD_Test access_BD_Test = new Access_BD_Test();

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        verifCoDECO();
        connecter(login, motPasse); 

        try {
            employesBD = access_BD_Test.getAllEmploye();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }
        

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        for (Employe employeVrf : employesBD) {
            clickOn(employeVrf.toString());
            clickOn("#btnConsultEmploye");

            TextField txtNom = find("#txtNom");
            assertEquals(employeVrf.nom, txtNom.getText());

            TextField txtPrenom = find("#txtPrenom");
            assertEquals(employeVrf.prenom, txtPrenom.getText());

            MenuButton mbtnDroit = find("#menuBtnDroitAccess");
            if (employeVrf.droitsAccess.equals("chefAgence")) {
                assertEquals("Chef d'agence", mbtnDroit.getText());
            } else {
                assertEquals("Guichetier", mbtnDroit.getText());
            }

            TextField txtLogin = find("#txtLogin");
            assertEquals(employeVrf.login, txtLogin.getText());

            TextField txtMotPasse = find("#txtMotPasse");
            assertEquals(employeVrf.motPasse, txtMotPasse.getText());

            clickOn("OK");
        }
    }

    @Test
    public void testSupprimerGuichetier() {
        resestBD();
        Assumptions.assumeTrue(test1Passed, "Test1 echec, impossible d'executer les autres tests");

        Access_BD_Test access_BD_Test = new Access_BD_Test();
        Employe employeBD = null;

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        verifCoDECO();
        connecter(login, motPasse); 

        try {
            employeBD = access_BD_Test.getEmploye("FP", "TheEnterprise");
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        clickOn(employeBD.toString());
        clickOn("#btnSuprEmploye");
        clickOn("Supprimer");

        assertEquals(null, find(employeBD.toString()));

        try {
            employeBD = access_BD_Test.getEmploye("FP", "TheEnterprise");
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        assertEquals(null, employeBD);
    }

    @Test
    public void testNouvelEmploye() {
        //resestBD();
        Assumptions.assumeTrue(test1Passed, "Test1 echec, impossible d'executer les autres tests");

        Access_BD_Test access_BD_Test = new Access_BD_Test();
        Employe employeBD = null;

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        verifCoDECO();
        connecter(login, motPasse);

        clickOn("Gestion");
        clickOn("#mitemEmploye");

        clickOn("#btnNouvelEmploye"); 

        //gtNbemploye
        

        String nomVrf = "Fournet";
        String prenomVrf = "Enzo";
        String droitVrf = "Guichetier";
        String loginVrf = "EF";
        String motPasseVrf = "enzo";


       


        ((TextField) find("#txtNom")).setText(nomVrf);
        ((TextField) find("#txtPrenom")).setText(prenomVrf);

        if (droitVrf.equals("Chef d'agence")) {
            clickOn("#menuBtnDroitAccess");
            clickOn("Chef d'agence");
        } else {
            clickOn("#menuBtnDroitAccess");
            clickOn("Guichetier");
        }

        ((TextField) find("#txtLogin")).setText(loginVrf);
        ((TextField) find("#txtMotPasse")).setText(motPasseVrf);

        clickOn("#butOk");

        int nbEmploye = 0;
        try {
            nbEmploye = access_BD_Test.getSeqEmplCurrVal();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        Employe nouvelEmpl = new Employe(nbEmploye, nomVrf, prenomVrf, droitVrf, loginVrf, motPasseVrf, dailyBankState.getAgenceActuelle().idAg);

        System.out.println(nouvelEmpl.toString());
        //[12]  FOURNET Enzo(EF)  {Guichetier}
        System.out.println(find(nouvelEmpl.toString()));

        sleep(10000);
        
        clickOn(nouvelEmpl.toString());

        sleep(10000);
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

        Node selectedNode;
        do {
            selectedNode = null;
            for (Node node : nodes) {
                if (!getStageFromNode(node).isFocused()) {
                    selectedNode = node;
                    break; // sortir de la boucle dès qu'on trouve un noeud non focusé
                }
            }

            if (selectedNode != null) { 
                nodes.remove(selectedNode);
            }
        } while (!nodes.isEmpty() && selectedNode != null);


        System.out.println("node restant");
        System.out.println("size" + nodes.size());
        for (Node node : nodes) {
            System.out.println(node);
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
