package knabe.esa.local_bean;

import static org.junit.Assert.*;
import knabe.esa.local_bean.Konto;
import knabe.esa.local_bean.Konto.UngedecktException;

import org.junit.Test;

public class KontoTest {

	@Test
	public void testSaldoAendern() throws UngedecktException {
		final Konto konto = new Konto();
		assertEquals(0, konto.getSaldo());
		konto.saldoAendern(1000);
		assertEquals(1000, konto.getSaldo());
		konto.saldoAendern(-600);
		assertEquals( 400, konto.getSaldo());
		konto.saldoAendern(-400);
		assertEquals(   0, konto.getSaldo());
		try {
			konto.saldoAendern(-1);
			fail("UngedecktException expected");
		} catch ( UngedecktException expected ){}
	}

}
