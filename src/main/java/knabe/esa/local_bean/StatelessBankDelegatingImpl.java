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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.embeddable.EJBContainer;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.List;


@Stateless 
//@TransactionManagement(TransactionManagementType.CONTAINER) //Default
//@TransactionAttribute(TransactionAttributeType.REQUIRED)    //Default
/**Example of an interface view of a stateless session bean.*/
public class StatelessBankDelegatingImpl implements StatelessBank {

    
	//Dependency injection by container:
    @EJB private StatefulBank bank;

    //@TransactionAttribute(TransactionAttributeType.REQUIRED) //Default
    /* (non-Javadoc)
	 * @see knabe.esa.local_bean.StatelessBank#kontoEroeffnen(java.lang.String, int)
	 */
    @Override
	public Konto kontoEroeffnen(final String name, final int saldo){
    	bank.anmelden(name);
    	return bank.kontoEroeffnen(saldo);
    }

    /* (non-Javadoc)
	 * @see knabe.esa.local_bean.StatelessBank#kontoSuchen(java.lang.String)
	 */
    @Override
	public Konto kontoSuchen(final String name) {
    	bank.anmelden(name);
        return bank.meinKonto();
    }
    
    /* (non-Javadoc)
	 * @see knabe.esa.local_bean.StatelessBank#ueberweisen(java.lang.String, java.lang.String, int)
	 */
    @Override
	public void ueberweisen(final String auftraggeber, final String empfaenger, final int betrag){
    	bank.anmelden(auftraggeber);
    	bank.ueberweisen(empfaenger, betrag);
    }
    
    /* (non-Javadoc)
	 * @see knabe.esa.local_bean.StatelessBank#kontoAufloesen(java.lang.String)
	 */
    @Override
	public void kontoAufloesen(final String name){
    	bank.anmelden(name);
    	bank.kontoAufloesen();
    }

    /* (non-Javadoc)
	 * @see knabe.esa.local_bean.StatelessBank#alleKonten()
	 */
    @Override
	public List<Konto> alleKonten(){
    	return bank.alleKonten();
    }

}
