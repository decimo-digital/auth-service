package it.decimo.auth_service.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.services.JwtUtils;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;

    public SecurityConfig(JwtUtils jwtUtils, ObjectMapper mapper) {
        this.jwtUtils = jwtUtils;
        this.mapper = mapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor(jwtUtils, mapper))
                .addPathPatterns("/api/**");
    }
}

/**
 * Intercetta tutte le chiamate per verificare che il JWT sia valido
 */
class JwtInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;

    public JwtInterceptor(JwtUtils jwtUtils, ObjectMapper mapper) {
        this.jwtUtils = jwtUtils;
        this.mapper = mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            var needLogin = ((HandlerMethod) handler).getMethodAnnotation(NeedLogin.class);
            if (needLogin == null) {
                needLogin = ((HandlerMethod) handler).getMethod().getDeclaringClass()
                        .getAnnotation(NeedLogin.class);
            }

            boolean canContinue = false;
            if (needLogin != null) {
                final var headers = request.getHeaders("access-token");
                if (headers.hasMoreElements()) {
                    do {
                        final var header = headers.nextElement();
                        canContinue = jwtUtils.isJwtValid(header);
                        break;
                    } while (headers.hasMoreElements());
                }
            } else {
                // Se arriva qua vuol dire che l'annotation NeedLogin non era impostata
                return true;
            }

            if (!canContinue) {
                response.setStatus(401);
                final var message = mapper.writeValueAsString(new BasicResponse("Missing access-token", "NO_ACCESS_TOKEN"));
                response.getWriter().print(message);
                response.setContentLength(message.length());
                response.setContentType("text/plain");
            }
        }
        logger.warn("Call to {} was denied because user wasn't logged in", request.getRequestURL());

        return false;
    }
}