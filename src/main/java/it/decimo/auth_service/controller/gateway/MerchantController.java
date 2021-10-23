package it.decimo.auth_service.controller.gateway;

import it.decimo.auth_service.utils.annotations.NeedLogin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@NeedLogin
@RestController
@RequestMapping(value = "/api/merchant")
public class MerchantController {
 
}
