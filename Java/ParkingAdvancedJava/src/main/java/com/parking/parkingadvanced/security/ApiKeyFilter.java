package com.parking.parkingadvanced.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.api.key}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Sadece sensörden gelen (veri yazan) endpoint'i korumaya alıyoruz
        if (requestURI.startsWith("/api/parking/sensor")) {

            // İsteğin Header (Başlık) kısmından şifreyi çekiyoruz
            String providedApiKey = request.getHeader("X-API-KEY");

            // Şifre yoksa veya yanlışsa 401 Unauthorized dönüp işlemi anında kesiyoruz
            if (providedApiKey == null || !providedApiKey.equals(expectedApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("401 Unauthorized: Gecersiz veya eksik API Key.");
                return; // Zinciri kır, Controller'a gitmesini engelle!
            }
        }

        // Şifre doğruysa veya istek korumasız bir GET endpoint'ine geldiyse yola devam et
        filterChain.doFilter(request, response);
    }
}