package knabe.esa.local_bean;

import javax.ejb.ApplicationException;

public class Bank {

	@ApplicationException(rollback=true)
	public static class NegativBetragException extends RuntimeException {
		public NegativBetragException(final String name, final int betrag){
			super("Konto " + name + ": Negativer Betrag " + betrag + " ist nicht erlaubt.");
		}
		private static final long serialVersionUID = 4695783733351823568L;
	}

}
