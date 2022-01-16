package it.decimo.auth_service.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.services.JwtUtils;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import it.decimo.auth_service.utils.exception.ExpiredJWTException;
import it.decimo.auth_service.utils.exception.InvalidJWTBody;
import it.decimo.auth_service.utils.exception.JWTUsernameNotExistingException;
import lombok.extern.slf4j.Slf4j;
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
        registry.addInterceptor(new JwtInterceptor(jwtUtils, mapper)).addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**");
    }
}

/**
 * Intercetta tutte le chiamate per verificare che il JWT sia valido
 */
@Slf4j
class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;

    public JwtInterceptor(JwtUtils jwtUtils, ObjectMapper mapper) {
        this.jwtUtils = jwtUtils;
        this.mapper = mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            var needLogin = ((HandlerMethod) handler).getMethodAnnotation(NeedLogin.class);

            if (needLogin == null) {
                needLogin = ((HandlerMethod) handler).getMethod().getDeclaringClass().getAnnotation(NeedLogin.class);
            }

            if (needLogin == null) {
                // Se arriva qua vuol dire che l'annotation NeedLogin non era impostata
                return true;
            }

            boolean canContinue = false;

            final var headers = request.getHeaders("access-token");
            if (headers.hasMoreElements()) {
                do {
                    final var header = headers.nextElement();
                    try {
                        canContinue = jwtUtils.isJwtValid(header);
                    } catch (ExpiredJWTException e) {
                        log.warn("Sent an expired JWT");
                    } catch (JWTUsernameNotExistingException e) {
                        log.warn("Username in jwt is non-existent");
                    } catch (InvalidJWTBody e) {
                        log.warn("The jwt sent wasn't parsificable");
                    }
                    break;
                } while (headers.hasMoreElements());
            }

            if (!canContinue) {
                response.setStatus(401);
                final var message = mapper
                        .writeValueAsString(new BasicResponse("Missing access-token", "NO_ACCESS_TOKEN"));
                response.getWriter().print(message);
                response.setContentLength(message.length());
                response.setContentType("text/plain");
                return false;
            }
        }
        return true;
    }
}