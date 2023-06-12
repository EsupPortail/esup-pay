/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.pay.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.StatsForm;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
	
	@Resource
	PayEvtDaoService payEvtDaoService;
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;

	public List<StatsForm> findListeStats(List<Object[]> listeStats){
		 List<StatsForm> listeObjets= new ArrayList<>();
		if(listeStats.size()!=0){
			for(Object[] o : listeStats){
				StatsForm statsForm = new StatsForm();
				statsForm.setTitle(o[0].toString());
				if(o[1]==null){
					o[1]="";
				}
				statsForm.setData1(o[1].toString());
				if(o.length>2) {
					if(o[2]==null){
						o[2]="";
					}
					statsForm.setData2(o[2].toString());
				}
				listeObjets.add(statsForm);
			}
		}
		
		return listeObjets;
	}
	
    public LinkedHashMap<String, Object> mapField(List<StatsForm> listes, int level){
    	
    	LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    	
    	LinkedHashMap<String, Object> secondMap = new LinkedHashMap<String, Object>();
    	
    	String test = null;
    	int i = 1;
    	
   		for (StatsForm statform : listes) {
   			if (level == 2){
	   		    map.put(statform.getTitle(),statform.getData1());
   			}else{
   				if(test== null || test.equals(statform.getTitle())){
   					secondMap.put(statform.getData1(),statform.getData2());
   				}else{
   					map.put(test, secondMap);
   					secondMap = new LinkedHashMap<String, Object>();
   					secondMap.put(statform.getData1(),statform.getData2());
   				}
   				test = statform.getTitle();
   				if(i ==  listes.size()){
   					map.put(test, secondMap);
   				}
   				i++;
   			}
   		}
        return map;
    }
    
    @SuppressWarnings("serial")
	public  LinkedHashMap<String,Object> getStats(String year) {
    	
		LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>() {
	        {
	            put("montants",mapField(findListeStats(payEvtDaoService.findSumMontantGroupByEvt(year)), 2));
	            put("participants",mapField(findListeStats(payEvtDaoService.findNbParticipantsByEvt(year)), 2));
	            put("transactions",mapField(findListeStats(payTransactionLogDaoService.findNbTransactionByYear()), 2));
	            put("cumul",mapField(findListeStats(payTransactionLogDaoService.findMontantByYear()), 2));
	            put("transactionsMonth",mapField(findListeStats(payTransactionLogDaoService.findNbTransactionByMonth()), 3));
	            put("cumulMonth",mapField(findListeStats(payTransactionLogDaoService.findMontantByMonth()), 3));
	        }

	    };
		return results;
    }

	public List<String> getDistinctYears() {
		return payTransactionLogDaoService.findDistinctYears();
	}
}
