package umastar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI umaStarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UmaStar API")
                        .version("v1")
                        .description("""
                                Racehorse Transport System Backend API.

                                Quy ước phân quyền trên Swagger:
                                - [Công khai]: Không cần Access Token (`register`, `login`, `refresh`, `logout`, `forgot-password`, `reset-password`).
                                - [Đã đăng nhập - Mọi role]: Cần Bearer JWT của bất kỳ tài khoản `ACTIVE` nào (`CUSTOMER`, `LOGISTICS_MANAGER`, `TRANSPORT_SPECIALIST`, `FLEET_COORDINATOR`, `DRIVER`, `ESCORT`).
                                - [LOGISTICS_MANAGER] / [FLEET_COORDINATOR]: Chỉ role được liệt kê mới gọi được (sai role trả `403 FORBIDDEN`)."""))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
