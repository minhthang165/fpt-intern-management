package com.fsoft.fintern.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoft.fintern.dtos.LoginUserDTO;
import com.fsoft.fintern.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class BannedUserFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            LoginUserDTO user = (LoginUserDTO) session.getAttribute("user");
            if (user != null) {
                String key = "banned_user:" + user.getId();
                String banInfoJson = (String) redisTemplate.opsForValue().get(key);

                if (banInfoJson != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> banInfo = mapper.readValue(banInfoJson, Map.class);
                        Long remainingTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);

                        // Pass attributes directly to the Thymeleaf template
                        request.setAttribute("reason", banInfo.get("reason"));
                        request.setAttribute("remainingDuration", remainingTime);
                        request.setAttribute("duration", banInfo.get("duration"));


                        request.getRequestDispatcher("/banned").forward(request, response);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace(); // Log any issues for debugging
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
