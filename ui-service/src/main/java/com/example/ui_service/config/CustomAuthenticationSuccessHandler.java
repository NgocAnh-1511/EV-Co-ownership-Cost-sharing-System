package com.example.ui_service.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger; // Thêm thư viện log (tùy chọn nhưng tốt)
import org.slf4j.LoggerFactory; // Thêm thư viện log (tùy chọn nhưng tốt)
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component // Đảm bảo lớp này là một Spring Bean
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // (Tùy chọn) Thêm logger để theo dõi dễ hơn thay vì System.out.println
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(authentication); // Xác định URL đích

        if (response.isCommitted()) {
            logger.warn("Phản hồi đã được gửi đi. Không thể chuyển hướng đến " + targetUrl);
            return;
        }

        // Ghi log để kiểm tra
        logger.info(">>> Đang chuyển hướng đến: {} với vai trò: {}", targetUrl, authentication.getAuthorities());

        redirectStrategy.sendRedirect(request, response, targetUrl); // Thực hiện chuyển hướng
    }

    /**
     * Xác định URL đích dựa trên vai trò của người dùng.
     * @param authentication Đối tượng chứa thông tin xác thực (bao gồm vai trò).
     * @return URL đích ("/admin/dashboard" hoặc "/checkin-checkout").
     */
    protected String determineTargetUrl(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Kiểm tra xem người dùng có vai trò ROLE_ADMIN không
        boolean isAdmin = authorities.stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")); // So sánh chính xác chuỗi "ROLE_ADMIN"

        if (isAdmin) {
            return "/admin/dashboard"; // Trả về URL trang admin
        } else {
            return "/contract-management";
            // Trả về URL trang user
        }
    }
}