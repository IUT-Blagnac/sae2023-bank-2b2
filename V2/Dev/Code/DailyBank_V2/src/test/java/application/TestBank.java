package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

import java.util.ArrayList;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

import application.control.DailyBankMainFrame;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import model.data.Employe;
import model.orm.Access_BD_Test;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;

/**
 *
 * Quelque test sur l'interface graphique de l'application.
 *
 * @author Enzo Fournet
 */
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

    /**
     * Méthode pour se connecter à l'application avant chaque test et la réinitialiser si ce n'est pas déjà fait
     * @author Enzo Fournet
     */
    @BeforeEach
    public void beforeEach() {
        //On réinitialise la BD si ce n'est pas déjà fait en utilisant la méthode resestBD
        this.resestBD();

        //On vérifie que le test1 est passé puisque les autres tests en dépendent
        Assumptions.assumeTrue(test1Passed, "Test1 echec, impossible d'executer les autres tests");

        //login
        String login = "Tuff";
        String motPasse = "Lejeune";

        //On verifie si l'on doit se déconnecter avec ka méthode verifCoDECO
        this.verifCoDECO();
        //On se connecte avec la méthode connecter
        this.connecter(login, motPasse);
    }

    @AfterEach
    public void afterEach() {
        this.release(new KeyCode[] {});
        this.release(new MouseButton[] {});
    }

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    /**
     * Méthode pour réinitialiser la BD si ce n'est pas déjà fait dans les tests en cours.
     * @author Enzo Fournet
     */
    public void resestBD() {
        if(!resestBD) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    IntegerProperty waitingTime = new SimpleIntegerProperty(7);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setMaxWidth(600);
                    stage.setMaxHeight(300);
                    alert.setHeaderText("Veuillez patienter la base de données est en cours de réinitialisation.\nN'intéragissez pas avec votre clavier ou votre souris pendant le processus de test.\nCette fenêtre se fermera automatiquement.");
                    alert.getButtonTypes().clear();
                    waitingTime.addListener((observable, oldValue, newValue) -> {
                    alert.setContentText("Les test s'executeront dans " + newValue.toString() + " secondes.");
                    });
                    alert.show();
                    Thread tache = new Thread( () -> {
                        try {
                            while (waitingTime.get() >= 0) {
                                Platform.runLater( () -> {
                                    if(waitingTime.get() == 0) {
                                        stage.close();
                                    }
                                    if(waitingTime.get() > 0) {
                                        waitingTime.set(waitingTime.get() - 1);
                                    }
                                });
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException e) {
                        }
                    });
                    tache.start();
                }
            });
            Access_BD_Test abt = new Access_BD_Test();
            try {
                abt.resestBD();
                resestBD = true;
                this.sleep(2000);
            } catch (DataAccessException | DatabaseConnexionException e) {
                e.printStackTrace();
                resestBD = false;
            }
        }
    }

    /**
     * Méthode pour se déconnecter de l'application si l'on est connecté
     * @author Enzo Fournet
     */
    public void verifCoDECO() {
        Button btnDeconn = this.find("#btnDeconn");
        if (btnDeconn.isVisible()) {
            this.clickOn("#btnDeconn");
        }
    }

    /**
     * Méthode pour se connecter à l'application
     * @author Enzo Fournet
     * @param login
     * @param motPasse
     */
    public void connecter(String login, String motPasse) {
        //lancer la feneêtre de connexion
        this.clickOn("#btnConn");

        //remplir les champs de login
        this.clickOn("#txtLogin").write(login);
        this.clickOn("#txtPassword").write(motPasse);
        this.clickOn("#btnValider");
    }



    /**
     * Méthode pour tester la connexion d'un employe et vérifier que les labels sont bien remplis avec les infos de l'employer <br/>
     * actuel et que l'employer dans dailyBankState est le même que celui de la BD
     * @author Enzo Fournet
     */
    @Test
    @Order(1)
    public void testLogin() {
        String login = "Tuff";
        String motPasse = "Lejeune";
        String prenom = "Michel";
        String nom = "Tuffery";

        //verifier que les labels sont bien remplis avec les infos de l'employer
        verifyThat("#lblEmpNom", LabeledMatchers.hasText(nom));
        verifyThat("#lblEmpPrenom", LabeledMatchers.hasText(prenom));

        //verifier que l'employer dans dailyBankState est le même
        try {
            assertEquals(TestBank.dailyBankState.getEmployeActuel().login, login);
            assertEquals(TestBank.dailyBankState.getEmployeActuel().motPasse, motPasse);
            assertEquals(TestBank.dailyBankState.getEmployeActuel().prenom, prenom);
            assertEquals(TestBank.dailyBankState.getEmployeActuel().nom, nom);
        } catch (AssertionError e) {
            test1Passed = false;
        }
    }

    /**
     * Méthode pour tester l'affichage de la liste des employe dans la liste view <br/>
     * de compteManagementController et vérifier que les employes affichés sont les mêmes que dans la BD
     * @author Enzo Fournet
     */
    @Test
    public void testListEmploye() {
        int nbEmployeBD = 0;
        int nbEmployeLV = 0;
        ArrayList<Employe> employesBD = null;
        ArrayList<Employe> employesLV = null;
        Employe employeBD = null;
        Employe employeLV = null;

        this.clickOn("Gestion");
        this.clickOn("#mitemEmploye");

        //récupérer le nombre d'employe dans la liste view affcihée
        ListView lvEmployes = this.find("#lvEmployes");
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

    /**
     * Méthode pour tester la modification d'un employe
     * @author Enzo Fournet
     */
    @Test
    public void testModifierUnEmploye() {
        Employe employesBD = null;


        this.clickOn("Gestion");
        this.clickOn("#mitemEmploye");

        Access_BD_Test access_BD_Test = new Access_BD_Test();
        try {
            employesBD = access_BD_Test.getEmploye("LN", "Levieux");
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        this.doubleClickOn(employesBD.toString());


        TextField txtNom = this.find("#txtNom");
        String nomVrf = txtNom.getText() + "test";
        txtNom.setText(nomVrf);

        TextField txtPrenom = this.find("#txtPrenom");
        String prenomVrf = txtPrenom.getText() + "test";
        txtPrenom.setText(prenomVrf);

        this.clickOn("Guichetier");
        this.clickOn("Chef d'agence");

        TextField txtLogin = this.find("#txtLogin");
        String loginVrf = txtLogin.getText() + "test";
        txtLogin.setText(loginVrf);

        TextField txtMotPasse = this.find("#txtMotPasse");
        String motPasseVrf = txtMotPasse.getText() + "test";
        txtMotPasse.setText(motPasseVrf);

        TextField txtConfrMotPasse = this.find("#txtConfrMotPasse");
        txtConfrMotPasse.setText(motPasseVrf);

        this.clickOn("Modifier");

        try {
            employesBD = access_BD_Test.getEmploye(loginVrf, motPasseVrf);
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        this.doubleClickOn(employesBD.toString());

        txtNom = this.find("#txtNom");
        assertEquals(nomVrf, txtNom.getText());

        txtPrenom = this.find("#txtPrenom");
        assertEquals(prenomVrf, txtPrenom.getText());

        MenuButton mbtnDroit = this.find("#menuBtnDroitAccess");
        assertEquals("Chef d'agence", mbtnDroit.getText());

        txtLogin = this.find("#txtLogin");
        assertEquals(loginVrf, txtLogin.getText());

        txtMotPasse = this.find("#txtMotPasse");
        assertEquals(motPasseVrf, txtMotPasse.getText());
    }

    /**
     * Méthode pour tester la consultation d'un employe
     * @author Enzo Fournet
     */
    @Test
    public void testConsulterEmployes() {
        ArrayList<Employe> employesBD = null;
        Access_BD_Test access_BD_Test = new Access_BD_Test();

        try {
            employesBD = access_BD_Test.getAllEmploye();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }


        this.clickOn("Gestion");
        this.clickOn("#mitemEmploye");

        for (Employe employeVrf : employesBD) {
            this.clickOn(employeVrf.toString());
            this.clickOn("#btnConsultEmploye");

            TextField txtNom = this.find("#txtNom");
            assertEquals(employeVrf.nom, txtNom.getText());

            TextField txtPrenom = this.find("#txtPrenom");
            assertEquals(employeVrf.prenom, txtPrenom.getText());

            MenuButton mbtnDroit = this.find("#menuBtnDroitAccess");
            if (employeVrf.droitsAccess.equals("chefAgence")) {
                assertEquals("Chef d'agence", mbtnDroit.getText());
            } else {
                assertEquals("Guichetier", mbtnDroit.getText());
            }

            TextField txtLogin = this.find("#txtLogin");
            assertEquals(employeVrf.login, txtLogin.getText());

            TextField txtMotPasse = this.find("#txtMotPasse");
            assertEquals(employeVrf.motPasse, txtMotPasse.getText());

            this.clickOn("OK");
        }
    }

    /**
     * Méthode pour tester la suppression d'un employe
     * @author Enzo Fournet
     */
    @Test
    public void testSupprimerGuichetier() {
        Access_BD_Test access_BD_Test = new Access_BD_Test();
        Employe employeBD = null;

        try {
            employeBD = access_BD_Test.getEmploye("FP", "TheEnterprise");
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        this.clickOn("Gestion");
        this.clickOn("#mitemEmploye");

        this.clickOn(employeBD.toString());
        this.clickOn("#btnSuprEmploye");
        this.clickOn("Supprimer");

        assertEquals(null, this.find(employeBD.toString()));

        try {
            employeBD = access_BD_Test.getEmploye("FP", "TheEnterprise");
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        assertEquals(null, employeBD);
    }

    /**
     * Méthode pour tester la création d'un employe
     * @author Enzo Fournet
     */
    @Test //pas encore fonctionnel
    public void testNouvelEmploye() {
        Access_BD_Test access_BD_Test = new Access_BD_Test();
        Employe employeBD = null;

        this.clickOn("Gestion");
        this.clickOn("#mitemEmploye");

        this.clickOn("#btnNouvelEmploye");

        String nomVrf = "Fournet";
        String prenomVrf = "Enzo";
        String droitVrf = "guichetier";
        String loginVrf = "EF";
        String motPasseVrf = "enzofournet";

        ((TextField) this.find("#txtNom")).setText(nomVrf);
        ((TextField) this.find("#txtPrenom")).setText(prenomVrf);

        if (droitVrf.equals("Chef d'agence")) {
            this.clickOn("#menuBtnDroitAccess");
            this.clickOn("Chef d'agence");
        } else {
            this.clickOn("#menuBtnDroitAccess");
            this.clickOn("Guichetier");
        }

        ((TextField) this.find("#txtLogin")).setText(loginVrf);
        ((TextField) this.find("#txtMotPasse")).setText(motPasseVrf);
        ((TextField) this.find("#txtConfrMotPasse")).setText(motPasseVrf);

        this.clickOn("#butOk");
        int nbEmploye = 0;
        try {
            nbEmploye = access_BD_Test.getSeqEmplCurrVal();
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        Employe nouvelEmpl = new Employe(nbEmploye, nomVrf, prenomVrf, droitVrf, loginVrf, motPasseVrf, dailyBankState.getAgenceActuelle().idAg);

        Platform.runLater(() -> {
            ListView lvEmployes = this.find("#lvEmployes");
            lvEmployes.scrollTo(lvEmployes.getItems().size() - 1);

            /*
            * il est impossible de récupérer le nouvel employe dans la liste view donc on vérifie que le dernier employe de la liste view
            * est le même que nouvelEmpl directement sans utiliser l'inteface
            */
            assertEquals(lvEmployes.getItems().get(lvEmployes.getItems().size() - 1).toString(), nouvelEmpl.toString());
        });
        try {
            employeBD = access_BD_Test.getEmploye(nouvelEmpl.login, nouvelEmpl.motPasse);
        } catch (Exception e) {
            assertEquals(true, false, e.toString());
            e.printStackTrace();
        }

        //on vérifie que l'employeBD est le même que nouvelEmpl pour être sure que les valeur présente dans la BD sont les bonnes
        assertEquals(employeBD.toString(), nouvelEmpl.toString());

    }

    /**
     * Méthode pour trouver un élément dans la fenêtre de test à partir de son id
     * @author Enzo Fournet
     * @param <T>
     * @param query
     * @return
     */
    public <T extends Node> T find(final String query) {
        Set<Node> nodes = this.lookup(query).queryAll();

        if (nodes.isEmpty()) {
            return null;
        }

        Node selectedNode;
        do {
            selectedNode = null;
            for (Node node : nodes) {
                if (!this.getStageFromNode(node).isFocused()) {
                    selectedNode = node;
                    break; // sortir de la boucle dès qu'on trouve un noeud non focusé
                }
            }

            if (selectedNode != null) {
                nodes.remove(selectedNode);
            }
        } while (!nodes.isEmpty() && selectedNode != null);

        return (T) nodes.iterator().next();
    }
 
    /**
     * Méthode qui permet de récupérer le stage à partir d'un noeud 
     * 
     * @author Enzo Fournet
     * @param node
     * @return
     */
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
