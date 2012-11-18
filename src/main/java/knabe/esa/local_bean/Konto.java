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

import javax.ejb.ApplicationException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Konto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private int saldo;

    public Konto() {
    }

    public Konto(String name, int saldo) throws UngedecktException {
    	if(name==null || name.isEmpty()){
    		throw new IllegalArgumentException("Attribut name für Konto fehlt.");
    	}
        this.name = name;
        saldoAendern(saldo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSaldo() {
        return saldo;
    }

    public void saldoAendern(int betrag) throws UngedecktException {
        final int result = this.saldo + betrag;
        if(result<0){
        	throw new UngedecktException(this.name, this.saldo, betrag);
        }
        this.saldo = result;
    }

    @ApplicationException(rollback=true)
    public static class UngedecktException extends RuntimeException {
		public UngedecktException(final String name, final int saldo, final int betrag){
    		super("Konto " + name + ": Eine Änderung des Saldos " + saldo + " um den Betrag " + betrag + " ist nicht gedeckt.");
    	}
		private static final long serialVersionUID = -3072973070943174618L;
    }
    
    public String toString(){
    	return "\nKonto { name=" + name + ", saldo=" + saldo + "}";
    }


}
