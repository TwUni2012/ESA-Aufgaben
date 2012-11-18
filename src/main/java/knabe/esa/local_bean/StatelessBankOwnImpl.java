/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knabe.esa.local_bean;

import java.io.Serializable;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import knabe.esa.local_bean.Bank.NegativBetragException;
import knabe.esa.local_bean.Konto.UngedecktException;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 *
 * @author tiloW7-2012
 */
@Stateless
public class StatelessBankOwnImpl implements StatelessBank, Serializable {

    @PersistenceContext(unitName = "bank-unit"/*, type = PersistenceContextType.TRANSACTION*/) //Default: type=TRANSACTION
    private EntityManager entityManager;

    @Override
    public Konto kontoEroeffnen(String name, int saldo) {
        final Konto konto = new Konto(name, saldo);
        entityManager.persist(konto);
        return konto;
    }

    @Override
    public Konto kontoSuchen(String name) {
        // TODO
        // query kann eine NoResultException werfen, wenn die Abfrage nichts sinnvolles liefert
        final CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Konto> c = qb.createQuery(Konto.class);
        final Root<Konto> konto = c.from(Konto.class);
        final Predicate condition = qb.equal(konto.get("name"), name);
        c.where(condition);
        final TypedQuery<Konto> q = entityManager.createQuery(c);
        final Konto result = q.getSingleResult();
        return result;
    }

    @Override
    public void ueberweisen(String auftraggeber, String empfaenger, int betrag) throws NegativBetragException, UngedecktException {
        if (betrag < 0) {
            throw new Bank.NegativBetragException(auftraggeber, betrag);
        }
        final Konto quellKonto = kontoSuchen(auftraggeber);
        final Konto zielKonto = kontoSuchen(empfaenger);//(Konto) entityManager.find(Konto.class, empfaenger);
        zielKonto.saldoAendern(+betrag);
        quellKonto.saldoAendern(-betrag);
       
    }

    @Override
    public void kontoAufloesen(String name) {
        final Konto konto = kontoSuchen(name);
        // TODO evt. noch auf null testen?=
//        System.out.println("###############" + konto);
        if(konto != null) {
            entityManager.remove(konto);
        } else {
        throw new IllegalArgumentException("Der Nutzer: " + name + " besitzt derzeit kein Konto.");
    }
    }

    @Override
    public List<Konto> alleKonten() {
        final TypedQuery<Konto> query = entityManager.createQuery("SELECT k FROM Konto AS k", Konto.class);
        return query.getResultList();
    }
}
