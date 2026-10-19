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

import jakarta.annotation.Resource;
import org.esupportail.pay.domain.RespLogin;
import org.esupportail.pay.repository.RespLoginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class RespLoginService {
    @Resource
    private RespLoginRepository respLoginRepository;

    @Transactional
    public RespLogin findOrCreateRespLogin(String login) {
        Assert.hasLength(login, "The login argument is required");
        var existing = respLoginRepository.findByLogin(login);
        if (!existing.isEmpty()) return existing.get(0);
        RespLogin respLogin = new RespLogin();
        respLogin.setLogin(login);
        return respLoginRepository.save(respLogin);
    }
}
