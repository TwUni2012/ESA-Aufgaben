package knabe.esa.local_bean;

import java.util.List;


import knabe.esa.local_bean.Konto.UngedecktException;

public interface StatelessBank {

	/**Eröffnet ein Konto mit dem angegebenen Namen und Anfangssaldo.*/
	Konto kontoEroeffnen(String name, int saldo);

	/**Liefert das Konto mit dem angegebenen Namen aus der Datenbank als Value Object.*/
	Konto kontoSuchen(String name);

	/**Überweist den angegebenen Betrag vom auftraggeber zum empfaenger.*/
	void ueberweisen(String auftraggeber, String empfaenger, int betrag) throws Bank.NegativBetragException, Konto.UngedecktException;

	/**Löst das Konto mit dem angegebenen Namen auf.*/
	void kontoAufloesen(String name);

	/**Liefert alle Konten aus der Datenbank als Value Objects.*/
	List<Konto> alleKonten();

}