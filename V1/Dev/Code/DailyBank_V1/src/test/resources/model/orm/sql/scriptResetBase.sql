DROP TABLE PrelevementAutomatique/ 
DROP TABLE AssuranceEmprunt/ 
DROP TABLE Emprunt/ 

DROP TABLE Operation/ 
DROP TABLE TypeOperation/ 
DROP TABLE CompteCourant/ 
DROP TABLE Client/ 
ALTER TABLE AgenceBancaire DROP CONSTRAINT fk_AgenceBancaire_Emp_ChefAg/ 
DROP TABLE Employe/ 
DROP TABLE AgenceBancaire/ 

CREATE TABLE AgenceBancaire(
	idAg NUMBER(3),
	nomAg VARCHAR(30),
	adressePostaleAg VARCHAR(50),
	idEmployeChefAg NUMBER(3),
	CONSTRAINT pk_AgenceBancaire PRIMARY KEY (idAg)	
)/ 

CREATE TABLE Employe(
	idEmploye NUMBER(3),
	nom VARCHAR(25),
	prenom VARCHAR(15),
	droitsAccess VARCHAR(50),
	login VARCHAR(8),
	motPasse VARCHAR(15),
	idAg NUMBER(3),
	CONSTRAINT pk_Employe PRIMARY KEY (idEmploye),
	CONSTRAINT fk_Employe_AgenceBancaire
		FOREIGN KEY (idAg) REFERENCES AgenceBancaire(idAg),
	CONSTRAINT nn_Employe_idAg CHECK (idAG IS NOT NULL)	
)/ 

ALTER TABLE AgenceBancaire
ADD CONSTRAINT fk_AgenceBancaire_Emp_ChefAg 
		FOREIGN KEY (idEmployeChefAg) REFERENCES Employe(idEmploye)/ 
		
CREATE TABLE Client(
	idNumCli NUMBER(5),
	nom VARCHAR(25),
	prenom VARCHAR(15),
	adressePostale VARCHAR(50),
	email VARCHAR(20),
	telephone CHAR(10),
	estInactif CHAR(1),
	idAg NUMBER(3),
	CONSTRAINT pk_Client PRIMARY KEY (idNumCli),
	CONSTRAINT fk_Client_AgenceBancaire
		FOREIGN KEY (idAg) REFERENCES AgenceBancaire(idAg),
	CONSTRAINT ck_Client_estInactif CHECK (estInactif IN ('O', 'N')),
	CONSTRAINT nn_Client_idAg CHECK (idAG IS NOT NULL)	
)/ 

CREATE TABLE CompteCourant(
	idNumCompte NUMBER(5), 
	debitAutorise NUMBER(4),
	solde DECIMAL(10,2),
	idNumCli NUMBER(5), 
	CONSTRAINT pk_CompteCourant PRIMARY KEY (idNumCompte),
	CONSTRAINT fk_CpteCourant_Client
		FOREIGN KEY (idNumCli) REFERENCES Client(idNumCli),
	CONSTRAINT nn_CpteCourant_idNumCli CHECK (idNumCli IS NOT NULL)
)/ 

CREATE TABLE TypeOperation(
	idTypeOp	VARCHAR(25),
	CONSTRAINT pk_TypeOperation PRIMARY KEY (idTypeOp)
)/ 

CREATE TABLE Operation(
	idOperation NUMBER(12), 
	montant DECIMAL(8,2),
	dateOp	DATE DEFAULT sysdate,
	dateValeur DATE,
	idNumCompte NUMBER(5), 
	idTypeOp VARCHAR(25),
	CONSTRAINT pk_Operation PRIMARY KEY (idOperation),
	CONSTRAINT fk_Operation_CompteCourant
		FOREIGN KEY (idNumCompte) REFERENCES CompteCourant(idNumCompte),
	CONSTRAINT fk_Operation_TypeOperation
		FOREIGN KEY (idTypeOp) REFERENCES TypeOperation(idTypeOp),
	CONSTRAINT nn_Operation_CpteCourant CHECK (idNumCompte IS NOT NULL),
	CONSTRAINT nn_Operation_TypeOp CHECK (idTypeOp IS NOT NULL)
)/ 
COMMIT/ 

DELETE FROM Operation/ 
DELETE FROM TypeOperation/ 
DELETE FROM CompteCourant/ 
DELETE FROM Client/ 
ALTER TABLE AgenceBancaire DISABLE CONSTRAINT fk_AgenceBancaire_Emp_ChefAg/ 
DELETE FROM Employe/ 
DELETE FROM AgenceBancaire/ 
ALTER TABLE AgenceBancaire ENABLE CONSTRAINT fk_AgenceBancaire_Emp_ChefAg/ 

INSERT INTO AgenceBancaire (idAg, nomAg, adressePostaleAg) VALUES (1, 'Agence Blagnac Centre', '2 rue Pasteur 31700 Blagnac')/ 
INSERT INTO AgenceBancaire (idAg, nomAg, adressePostaleAg) VALUES (2, 'Agence Vieux Beauzelle', '14 rue de la république, 31700 Beauzelle')/ 
INSERT INTO AgenceBancaire (idAg, nomAg, adressePostaleAg) VALUES (3, 'Agence Blagnac Ouest', '4 place brassens, 31700 Blagnac')/ 

DROP SEQUENCE seq_id_employe/ 
CREATE SEQUENCE seq_id_employe
  MINVALUE 1  MAXVALUE 999999999999  
  START WITH 1 INCREMENT BY 1/ 
  

INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Tuffery','Michel','chefAgence','Tuff','Lejeune', 1)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Crampes','Jean-Bernard','chefAgence','JBC','Basse', 2)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Inglebert','Jean-Michel','chefAgence','JMI','Belote', 3)/ 

UPDATE AgenceBancaire
	SET idEmployeChefAg = 1 
	WHERE idAg = 1/ 
UPDATE AgenceBancaire
	SET idEmployeChefAg = 2
	WHERE idAg = 2/ 
UPDATE AgenceBancaire
	SET idEmployeChefAg = 3
	WHERE idAg = 3/ 


INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Nonne','Laurent','guichetier','LN','Levieux',1)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Teste','Olivier','guichetier','OT','Lemoyen',1)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Peninou','André','guichetier','AP','TheVoice',2)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Pelleau','Fabrice','guichetier','FP','TheEnterprise',2)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Demichiel','Marianne','guichetier','MDM','TheGiant',3)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Redon','Laurence','guichetier','LR','MissCobol',3)/ 
INSERT INTO Employe VALUES (seq_id_employe.NEXTVAL,'Pendaries','Esther','guichetier','EP','Paganini',3)/ 

DROP SEQUENCE seq_id_client/ 
CREATE SEQUENCE seq_id_client
  MINVALUE 1  MAXVALUE 999999999999  
  START WITH 1 INCREMENT BY 1/ 
  
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Gabin','Jean','3 rue t''as de beaux yeux tu sais, 31700 Blagnac','gabin@free.fr','0512345678','N',1)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Belmondo','Jean-Paul','4 rue des cascades, 31700 Blagnac','belmondo@gmail.com','0598765432','N',1)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Delon','Alain','34 rue du beau gosse, 31700 Blagnac','delon@gmail.com','0512457896','N',1)/ 

INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Ventura','Lino','3 rue des baffes, 31000 Toulouse','ventura@free.fr','0635785215','O',2)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Montand','Yves','7 avenue de la fille du facteur, 31000 Toulouse','montand@free.fr','0612395415','N',2)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'De Funes','Louis','3 avenue de ma biche, 31700 Beauzelle','funes@free.fr','0648565415','N',2)/ 

INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Bourvil','','4 impasse de la cuisine au beurre, 31700 Blagnac','bourvil@free.fr','0914265415','N',3)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Blier','Bernard','134 rue de la ventilation, 31700 Blagnac','blier@free.fr','0514265415','N',3)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Dépardieu','Gérard','134 rue des valseuses, 102151 Moscou','depardieu@kremlin.fr','0914265415','N',3)/ 
INSERT INTO Client VALUES (seq_id_client.NEXTVAL,'Réno','Jean','13 rue des nettoyeurs, 31700 Beauzelle','reno@yahoo.fr','0814765415','N',3)/ 

INSERT INTO TypeOperation VALUES ('Dépôt Espèces')/  
INSERT INTO TypeOperation VALUES ('Retrait Espèces')/  
INSERT INTO TypeOperation VALUES ('Dépôt Chèque')/  
INSERT INTO TypeOperation VALUES ('Paiement Chèque')/ 
INSERT INTO TypeOperation VALUES ('Retrait Carte Bleue')/  
INSERT INTO TypeOperation VALUES ('Paiement Carte Bleue')/  
INSERT INTO TypeOperation VALUES ('Virement Compte à Compte')/  
INSERT INTO TypeOperation VALUES ('Prélèvement automatique')/  
INSERT INTO TypeOperation VALUES ('Prélèvement agios')/   

DROP SEQUENCE seq_id_operation/ 
CREATE SEQUENCE seq_id_operation
  MINVALUE 1  MAXVALUE 999999999999  
  START WITH 1 INCREMENT BY 1/ 

DROP SEQUENCE seq_id_compte/ 
CREATE SEQUENCE seq_id_compte
  MINVALUE 1  MAXVALUE 999999999999  
  START WITH 1 INCREMENT BY 1/ 
  
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -200, 200, 1)/   
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 200, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, 0,    200, 1)/   
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 200, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 

INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -200,  100, 2)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 100, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -3000, 300, 3)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 300, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 

INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -150,  200, 4)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 200, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -800,  50, 5)/ 
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 50, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -50,   50, 6)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 50, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 

INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -850,  250, 7)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 250, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, -100,  300, 8)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 300, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, 0,     800, 9)/  
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 800, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 
	
INSERT INTO CompteCourant VALUES (seq_id_compte.NEXTVAL, 0,     1200, 10)/     
INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
	VALUES (seq_id_operation.NEXTVAL, 1200, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces')/ 

CREATE OR REPLACE PROCEDURE Debiter	(
	vidNumCompte CompteCourant.idNumCompte%TYPE,
	vMontantDebit Operation.montant%TYPE,
	vTypeOp TypeOperation.idTypeOp%TYPE,
	retour OUT NUMBER)
IS
	vDebitAutorise CompteCourant.debitAutorise%TYPE;
	vSolde CompteCourant.solde%TYPE;
	vNouveauSolde CompteCourant.solde%TYPE;
	
BEGIN
	SELECT debitAutorise, solde into vDebitAutorise, vSolde FROM CompteCourant WHERE idNumCompte = vidNumCompte;
	vNouveauSolde := vSolde - vMontantDebit;
	IF vNouveauSolde >= vDebitAutorise THEN
		INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
		VALUES (seq_id_operation.NEXTVAL, -vMontantDebit, sysdate +2, vidNumCompte, vTypeOp);

		UPDATE CompteCourant
		SET solde = vNouveauSolde
		WHERE idNumCompte = vidNumCompte;
		
		COMMIT;
		retour := 0;
	
	ELSE
		retour := -1;
	END IF;
	
END;
/ 

EXECUTE Debiter(1, TO_NUMBER('15,2'), 'Retrait Espèces', :ret)/ 


EXECUTE Debiter(1, TO_NUMBER('500'), 'Retrait Espèces', :ret)/ 





EXECUTE Debiter(1, 120, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(1, 40, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(1, 2, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(1, 157, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(2, 188, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(3, 97, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(4, 7, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(5, 58, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(6, 7, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(7, 27, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(8, 100, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(9, 5, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(10, 27, 'Retrait Espèces', :ret)/  
EXECUTE Debiter(11, 47, 'Retrait Espèces', :ret)/  

COMMIT/ 

ALTER TABLE CompteCourant
ADD (estCloture CHAR(1))/ 
	 
ALTER TABLE CompteCourant
ADD CONSTRAINT ck_CpteCour_estCloture CHECK (estCloture IN ('O', 'N'))/ 

UPDATE CompteCourant
SET estCloture = 'N'/ 

UPDATE CompteCourant
SET estCloture = 'O'
WHERE idNumCli = 4/ 

CREATE OR REPLACE PROCEDURE CreerOperation(
	vidNumCompte CompteCourant.idNumCompte%TYPE,
	vMontantOp Operation.montant%TYPE,
	vTypeOp TypeOperation.idTypeOp%TYPE,
	retour OUT NUMBER
	)
IS
    vDebitAutorise CompteCourant.debitAutorise%TYPE;
	vSolde CompteCourant.solde%TYPE;
	vNouveauSolde CompteCourant.solde%TYPE;
	
BEGIN
	SELECT debitAutorise, solde into vDebitAutorise, vSolde FROM CompteCourant WHERE idNumCompte = vidNumCompte;
	
	vNouveauSolde := vSolde + vMontantOp;
	IF vNouveauSolde >= vDebitAutorise THEN
	
		INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
		VALUES (seq_id_operation.NEXTVAL, vMontantOp, sysdate +2, vidNumCompte, vTypeOp);

		UPDATE CompteCourant
		SET solde = vNouveauSolde
		WHERE idNumCompte = vidNumCompte;
	
		COMMIT;
		retour := 0;
	
	ELSE
		retour := -1;
	END IF;
END;
/ 




EXECUTE CreerOperation(1, TO_NUMBER('100,25'), 'Dépôt Chèque', :ret)/  
EXECUTE CreerOperation(1, TO_NUMBER('-25'), 'Retrait Espèces', :ret)/  




CREATE OR REPLACE PROCEDURE CreerCompte(
	vDebitAutorise CompteCourant.debitAutorise%TYPE,
	vMontantInitial Operation.montant%TYPE,
	vIdNumCli CompteCourant.idNumCli%TYPE, 
	retour OUT NUMBER
	)
IS

BEGIN
	IF vMontantInitial < 50 THEN
		retour := -1;
	ELSE
		INSERT INTO CompteCourant (idNumCompte, debitAutorise, solde, idNumcli, estCloture) 
		VALUES (seq_id_compte.NEXTVAL, vDebitAutorise, vMontantInitial, vIdNumCli, 'N');  
		
		INSERT INTO Operation (idOperation, montant, dateValeur, idNumCompte, idTypeOp)
		VALUES (seq_id_operation.NEXTVAL, vMontantInitial, sysdate +2, seq_id_compte.CURRVAL, 'Dépôt Espèces');
		
		retour := seq_id_compte.CURRVAL;
		COMMIT;
	END IF;
END;
/ 




EXECUTE CreerCompte(TO_NUMBER('-300'), TO_NUMBER('120'), 2, :ret)/  





EXECUTE CreerCompte(TO_NUMBER('-300'), TO_NUMBER('20'), 2, :ret)/  


CREATE OR REPLACE PROCEDURE Virer(
	vIdNumCompteDeb CompteCourant.idNumCompte%TYPE,
	vIdNumCompteCred CompteCourant.idNumCompte%TYPE,
	vMontantOp Operation.montant%TYPE,
	retour OUT NUMBER)
IS
 
BEGIN
	CreerOperation(vIdNumCompteDeb, -vMontantOp, 'Virement Compte à Compte', retour);
	IF retour = 0 THEN 
	      CreerOperation(vIdNumCompteCred, vMontantOp, 'Virement Compte à Compte', retour);
		  COMMIT;
	END IF;
	
END;
/ 



EXECUTE Virer(2, 1, TO_NUMBER('97,23'), :ret)/  






EXECUTE Virer(2, 1, TO_NUMBER('11,23'), :ret)/  


COMMIT/ 

CREATE TABLE Emprunt(
	idEmprunt NUMBER(5),
	tauxEmp DECIMAL(4,2), 
	capitalEmp NUMBER(8), 
	dureeEmp NUMBER(3), 
	dateDebEmp DATE, 
	idNumCli NUMBER(5),
	CONSTRAINT pk_Emprunt PRIMARY KEY (idEmprunt),
	CONSTRAINT fk_Emprunt_Client
		FOREIGN KEY (idEmprunt) REFERENCES Client(idNumCli),
	CONSTRAINT nn_Emprunt_idNumCli CHECK (idNumCli IS NOT NULL)	
)/ 

CREATE TABLE AssuranceEmprunt(
	idAss NUMBER(5),
	tauxAss DECIMAL(4,2), 
	tauxCouv DECIMAL(5,2), 
	idEmprunt NUMBER(5),
	CONSTRAINT pk_AssuranceEmprunt PRIMARY KEY (idAss),
	CONSTRAINT fk_AssurEmp_Emprunt
		FOREIGN KEY (idEmprunt) REFERENCES Emprunt(idEmprunt),
	CONSTRAINT nn_AssurEmp_idEmprunt CHECK (idEmprunt IS NOT NULL)	
)/ 

DROP SEQUENCE seq_id_emprunt/ 
CREATE SEQUENCE seq_id_emprunt
  MINVALUE 1  MAXVALUE 999999999999  
  START WITH 1 INCREMENT BY 1/ 
  
DROP SEQUENCE seq_id_assurance/ 
CREATE SEQUENCE seq_id_assurance
  MINVALUE 1  MAXVALUE 999999999999  
  START WITH 1 INCREMENT BY 1/ 

INSERT INTO Emprunt VALUES (seq_id_emprunt.NEXTVAL, 4.5, 250000, 240, '01/09/2019', 2)/ 
INSERT INTO Emprunt VALUES (seq_id_emprunt.NEXTVAL, 2.2, 50000, 48, '01/06/2019', 9)/ 
INSERT INTO AssuranceEmprunt VALUES (1, 0.85, 100, seq_id_emprunt.CURRVAL)/ 
	

DROP SEQUENCE seq_id_prelevAuto/ 
CREATE SEQUENCE seq_id_prelevAuto
  MINVALUE 1  MAXVALUE 99999999 
  START WITH 1 INCREMENT BY 1/ 
  

 
CREATE TABLE PrelevementAutomatique(
	idPrelev NUMBER(8), 
	montant DECIMAL(8,2),
	dateRecurrente NUMBER(2), 
	beneficiaire VARCHAR(50), 
	idNumCompte NUMBER(5),
	CONSTRAINT pk_PrelevAuto PRIMARY KEY (idPrelev),
	CONSTRAINT fk_PrelevAuto_CompteCourant
		FOREIGN KEY (idNumCompte) REFERENCES CompteCourant(idNumCompte),
	CONSTRAINT nn_PrelevAuto_CpteCourant CHECK (idNumCompte IS NOT NULL)
)/ 

INSERT INTO PrelevementAutomatique VALUES (seq_id_prelevAuto.NEXTVAL, 120, 5, 'EDF', 1)/ 
INSERT INTO PrelevementAutomatique VALUES (seq_id_prelevAuto.NEXTVAL, 55,  3, 'Free Telecom', 1)/ 

COMMIT/ 

INSERT INTO PrelevementAutomatique VALUES (seq_id_prelevAuto.NEXTVAL, 100, 23, 'GDF', 1)/ 
INSERT INTO PrelevementAutomatique VALUES (seq_id_prelevAuto.NEXTVAL, 50,  23, 'Orange', 1)/ 
INSERT INTO PrelevementAutomatique VALUES (seq_id_prelevAuto.NEXTVAL, 212, 23, 'Véolia', 1)/ 

CREATE OR REPLACE PROCEDURE ExecuterPrelevAuto (retour OUT VARCHAR)
IS
	CURSOR c1 IS SELECT * FROM PrelevementAutomatique WHERE dateRecurrente = TO_CHAR(SYSDATE, 'DD');
	testDebit NUMBER := 0;
	
BEGIN
	FOR C1_ligne IN C1 LOOP
	    Debiter(C1_ligne.idNumCompte, C1_ligne.montant, 'Prélèvement automatique', testDebit);
		IF testDebit = -1 THEN	
			retour := retour || 'Problème sur le debit du prelev auto num '|| C1_ligne.idPrelev || ' du montant ' || C1_Ligne.montant 
							  || ' sur le compte '|| C1_Ligne.idNumCompte ||'    -    ';
		ELSE 					  
			DBMS_OUTPUT.PUT_LINE('Execution du prelev auto num '|| C1_ligne.idPrelev || ' du montant ' || C1_Ligne.montant 
							  || ' sur le compte '|| C1_Ligne.idNumCompte);
		END IF;
	END LOOP;
END;
/ 

COMMIT/ 

