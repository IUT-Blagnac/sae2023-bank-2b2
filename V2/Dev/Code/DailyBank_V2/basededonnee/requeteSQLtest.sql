SELECT * FROM EMPLOYE;

SELECT count(*) as nbEmploye FROM EMPLOYE;


SELECT seq_id_employe.nextval FROM DUAL;
select seq_id_employe.currval from dual;

SELECT * FROM operation where idnumcompte = 1 order by idoperation;

SHOW ERRORS PROCEDURE Crediter;
SHOW ERRORS PROCEDURE Debiter;
SHOW ERRORS PROCEDURE effectuerVirement;