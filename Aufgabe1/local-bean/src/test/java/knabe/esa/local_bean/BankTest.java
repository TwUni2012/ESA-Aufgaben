/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package knabe.esa.local_bean;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import knabe.esa.local_bean.Bank.NegativBetragException;
import knabe.esa.local_bean.Konto;
import knabe.esa.local_bean.Konto.UngedecktException;

/**Test case for the EJBs StatefulBank and StatelessBank. Knabe 2012-09-25*/
public class BankTest extends Assert {
	
	private static EJBContainer _ejbContainer = null;

	@BeforeClass
    public static void startEmbeddedContainer() throws Exception {
        final Map<String,String> properties = new HashMap<String,String>(){
			private static final long serialVersionUID = 1L;
			{
				//Defaults in OpenEJB. So the following can be left out.
		        put("bankDatabase", "new://Resource?type=DataSource");
		        put("bankDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
		        put("bankDatabase.JdbcUrl", "jdbc:hsqldb:mem:bankdb");
	        }
		};
        _ejbContainer = EJBContainer.createEJBContainer(/*properties*/);
    }
	
	@AfterClass
    public static void stopEmbeddedContainer() throws Exception {
        _ejbContainer.close();
        _ejbContainer = null;
    }	
	
	@Test
    public void statefulBankEJB() throws Exception {
		final Context jndiContext = _ejbContainer.getContext();
        final String jndiName = "java:global/local-bean/StatefulBank";
		final StatefulBank thomasBank = (StatefulBank) jndiContext.lookup(jndiName);
        final StatefulBank heikeBank = (StatefulBank) jndiContext.lookup(jndiName);
        //Proxy-Name bei OpenEJB, nicht portabel:
        assertEquals("StatefulBank$LocalBeanProxy", thomasBank.getClass().getSimpleName());
        thomasBank.anmelden("Thomas");
        heikeBank.anmelden("Heike");
        
        thomasBank.kontoEroeffnen(2000);
        heikeBank.kontoEroeffnen(4321);

		assertEquals("thomasBank.meinKonto().getSaldo()", 2000, thomasBank.meinKonto().getSaldo());
        assertEquals( "heikeBank.meinKonto().getSaldo()", 4321,  heikeBank.meinKonto().getSaldo());
        
        // Erlaubte Überweisung:
		thomasBank.ueberweisen("Heike", 2000);
		assertEquals("thomasBank.meinKonto().getSaldo()",    0, thomasBank.meinKonto().getSaldo());
		assertEquals( "heikeBank.meinKonto().getSaldo()", 6321,  heikeBank.meinKonto().getSaldo());
        
        //Ausnahme und Rollback bei unerlaubten Überweisungen:
		try {
			thomasBank.ueberweisen("Heike", -1);
			fail("Bank.NegativBetragException expected");
		} catch ( Bank.NegativBetragException expected ){}
		assertEquals("thomasBank.meinKonto().getSaldo()",    0, thomasBank.meinKonto().getSaldo());
		assertEquals( "heikeBank.meinKonto().getSaldo()", 6321,  heikeBank.meinKonto().getSaldo());
		try {
			thomasBank.ueberweisen("Heike", +1);
			fail("UngedecktException expected");
		} catch ( UngedecktException expected ){}
		assertEquals("thomasBank.meinKonto().getSaldo()",    0, thomasBank.meinKonto().getSaldo());
		assertEquals( "heikeBank.meinKonto().getSaldo()", 6321,  heikeBank.meinKonto().getSaldo());

        thomasBank.kontoAufloesen();
        heikeBank.kontoAufloesen();
        try {
        	thomasBank.meinKonto();
        	fail("EJBException expected");
        } catch ( EJBException expected ){}
        try {
        	heikeBank.meinKonto();
        	fail("EJBException expected");
        } catch ( EJBException expected ){}
    }

	@Test
    public void statelessBankEJB() throws Exception {
        final StatelessBank bank = (StatelessBank) _ejbContainer.getContext().lookup(
            //"java:global/local-bean/StatelessBankOwnImpl"
        	"java:global/local-bean/StatelessBankDelegatingImpl"
        );

        bank.kontoEroeffnen("Thomas", 2000);
        bank.kontoEroeffnen("Heike", 4321);

        final List<Konto> konten = bank.alleKonten();
        assertEquals("List.size()", 2, konten.size());
        System.out.println("Alle Konten:" + konten);

		assertEquals("bank.kontoSuchen(Thomas).getSaldo()", 2000, bank.kontoSuchen("Thomas").getSaldo());
        assertEquals("bank.kontoSuchen(Heike).getSaldo()", 4321, bank.kontoSuchen("Heike").getSaldo());
        
        // Erlaubte Überweisung:
		bank.ueberweisen("Thomas", "Heike", 2000);
		assertEquals("bank.kontoSuchen(Thomas).getSaldo()", 0, bank.kontoSuchen("Thomas").getSaldo());
		assertEquals("bank.kontoSuchen(Heike).getSaldo()", 6321, bank.kontoSuchen("Heike").getSaldo());
        
        //Ausnahme und Rollback bei unerlaubten Überweisungen:
		try {
			bank.ueberweisen("Thomas", "Heike", -1);
			fail("NegativBetragException expected");
		} catch ( Bank.NegativBetragException expected ){}
		assertEquals("bank.kontoSuchen(Thomas).getSaldo()", 0, bank.kontoSuchen("Thomas").getSaldo());
		assertEquals("bank.kontoSuchen(Heike).getSaldo()", 6321, bank.kontoSuchen("Heike").getSaldo());
		try {
			bank.ueberweisen("Thomas", "Heike", +1);
			fail("UngedecktException expected");
		} catch ( UngedecktException expected ){}
		assertEquals("bank.kontoSuchen(Thomas).getSaldo()", 0, bank.kontoSuchen("Thomas").getSaldo());
		assertEquals("bank.kontoSuchen(Heike).getSaldo()", 6321, bank.kontoSuchen("Heike").getSaldo());

        for (final Konto konto: konten) {
            bank.kontoAufloesen(konto.getName());
        }

        assertEquals("bank.alleKonten().size()", 0, bank.alleKonten().size());
    }

}
