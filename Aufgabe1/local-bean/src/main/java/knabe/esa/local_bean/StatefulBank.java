package knabe.esa.local_bean;

import javax.ejb.ApplicationException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.lang.reflect.Method;
import java.util.List;


@Stateful //Jeder Klient erhält eine eigene Instanz der Session Bean
//@TransactionManagement(TransactionManagementType.CONTAINER) //Default
//@TransactionAttribute(TransactionAttributeType.REQUIRED)    //Default
/**Example of a no-interface view of a stateful session bean.*/
public class StatefulBank {

    @PersistenceContext(unitName = "bank-unit"/*, type = PersistenceContextType.TRANSACTION*/) //Default: type=TRANSACTION
    private EntityManager entityManager;
    
    /**Name des aktuell angemeldeten Kontoinhabers.*/
	private String angemeldetName;
    
    //@TransactionAttribute(TransactionAttributeType.REQUIRED) //Default
    
    public void anmelden(final String name){
    	this.angemeldetName = name;
    }
    
    public Konto kontoEroeffnen(final int saldo){
    	_angemeldetPruefen();
    	final Konto konto = new Konto(angemeldetName, saldo);
        entityManager.persist(konto);
        return konto;
    }
    
    /**Liefert das Konto des angemeldeten Benutzers.*/
    public Konto meinKonto(){
    	_angemeldetPruefen();
    	return _kontoSuchen(this.angemeldetName);
    }

    /**Liefert das Konto mit dem angegebenen Namen aus der Datenbank als Value Object.*/
    private Konto _kontoSuchen(final String name) {
    	//Type Safe Query mit JPA 2, see http://www.ibm.com/developerworks/java/library/j-typesafejpa/
        final CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Konto> c = qb.createQuery(Konto.class);
        final Root<Konto> konto = c.from(Konto.class);
        final Predicate condition = qb.equal(konto.get("name"), name);
        c.where(condition);
        final TypedQuery<Konto> q = entityManager.createQuery(c);
        final Konto result = q.getSingleResult();
        return result;
    }
    
    public void ueberweisen(final String empfaenger, final int betrag){
    	_angemeldetPruefen();
    	if(betrag < 0){
    		throw new Bank.NegativBetragException(angemeldetName, betrag);
    	}
        final Konto quellKonto = meinKonto();
        final Konto zielKonto = _kontoSuchen(empfaenger);
        zielKonto .saldoAendern(+betrag);
        quellKonto.saldoAendern(-betrag);        
    }

    /**Liefert alle Konten aus der Datenbank als Value Objects mittels JPQL.*/
    public List<Konto> alleKonten(){
    	final TypedQuery<Konto> query = entityManager.createQuery("SELECT k FROM Konto AS k", Konto.class);
        return query.getResultList();
    }
    
    /**Prüft, dass aktuell ein Benutzer angemeldet ist.*/
    private void _angemeldetPruefen() throws NichtAngemeldetException {
    	final String n = this.angemeldetName;
    	if(n==null || n.isEmpty()){
    		throw new NichtAngemeldetException();
    	}
    }
    
    @ApplicationException(rollback=true)
    public static class NichtAngemeldetException extends RuntimeException {
		public NichtAngemeldetException(){
    		super("Benutzer nicht angemeldet. Konto-Operation nicht möglich.");
    	}
		private static final long serialVersionUID = 6785272904628072726L;
    }

    /**Löst das Konto des angemeldeten Benutzers auf.*/
    public void kontoAufloesen(){
        final Konto konto = meinKonto();
        entityManager.remove(konto);
    }
    
    /**Interceptor-Methode (einfache Aspektorientierung): Protokolliert jeden Methodenaufruf dieses Session Beans*/
    @AroundInvoke public Object logMethods(final InvocationContext ic) throws Exception {
    	final Method method = ic.getMethod();
    	final Object[] parameters = ic.getParameters();
    	final StringBuilder out = new StringBuilder();
    	if(angemeldetName!=null){
    		out.append(angemeldetName);
    		out.append(": ");
    	}
    	out.append(method.getName());
    	out.append('(');
    	boolean needsComma = false;
    	for(final Object p: parameters){
    		if(needsComma){out.append(", ");}
    		out.append(p);
    		needsComma = true;
    	}
    	out.append(')');
    	final Object result;
    	try {
			result = ic.proceed();
		} catch (Exception ex) {
			out.append(" throwed ");
			out.append(ex.getMessage());
	    	System.out.println(out);
			throw ex;
		}
    	if(result!=null){
    		out.append(" --> ");
    		out.append(result);
    	}
    	System.out.println(out);
		return result;
    }

}
