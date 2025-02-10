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
package org.esupportail.pay.web.admin;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.domain.UploadFile;
import org.esupportail.pay.services.ExportService;
import org.esupportail.pay.services.VentilationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/ventilations")
@Controller
@Transactional
public class VentilationController {

	private final Logger log = Logger.getLogger(getClass());
	
	@Resource
	VentilationService ventilationService;
	
	@Resource
	ExportService exportService;
	
	@RequestMapping
    public String getVentilations(Model uiModel, @RequestParam(required=false) @DateTimeFormat(pattern = "MM.yyyy") Date dateMonth) {
		if(dateMonth == null) {
			dateMonth = getActualMonth();
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.yyyy");
		uiModel.addAttribute("ventilations", ventilationService.getVentilations(dateMonth));
		uiModel.addAttribute("dateMonthBefore", simpleDateFormat.format(DateUtils.addMonths(dateMonth,-1)));
		uiModel.addAttribute("dateMonthAfter", simpleDateFormat.format(DateUtils.addMonths(dateMonth,+1)));
		uiModel.addAttribute("dateMonth", simpleDateFormat.format(dateMonth));
    	return "admin/ventilations/index";
    }
	
	@RequestMapping(value = "/addExportTransactionFile", method = RequestMethod.POST, produces = "text/html")
	public String addExportTransactionFile(UploadFile uploadFile) throws IOException, ParseException {
		exportService.consumeExportTransactionCsvFile(uploadFile.getLogoFile().getInputStream());
		return "redirect:/admin/ventilations";
	}
	
	@RequestMapping(value = "/addExportRemiseFile", method = RequestMethod.POST, produces = "text/html")
	public String addExportRemiseFile(UploadFile uploadFile) throws IOException, ParseException {
		exportService.consumeExportRemiseCsvFile(uploadFile.getLogoFile().getInputStream());
		return "redirect:/admin/ventilations";
	}
	
	Date getActualMonth() {
		 Calendar c = Calendar.getInstance();   
		 c.set(Calendar.DAY_OF_MONTH, 1);
		 c.set(Calendar.HOUR_OF_DAY, 0);
		 c.set(Calendar.MINUTE, 0);
		 c.set(Calendar.SECOND, 0);
		 return c.getTime();
	}
	
}