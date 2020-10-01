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
import java.io.InputStream;
import java.text.ParseException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.UploadFile;
import org.esupportail.pay.services.ExportService;
import org.esupportail.pay.services.VentilationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin/ventilations")
@Controller
public class VentilationController {

	private final Logger log = Logger.getLogger(getClass());
	
	@Resource
	VentilationService ventilationService;
	
	@Resource
	ExportService exportService;
	
	@RequestMapping
    public String getVentilations(Model uiModel) {
		uiModel.addAttribute("ventilations", ventilationService.getVentilations());
    	return "admin/ventilations";
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
	
}
