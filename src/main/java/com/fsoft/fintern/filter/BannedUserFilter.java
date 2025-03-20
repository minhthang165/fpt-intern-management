//package com.fsoft.fintern.filter;
//
//import com.fsoft.fintern.dtos.LoginUserDTO;
//import com.fsoft.fintern.models.User;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class BannedUserFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            LoginUserDTO user = (LoginUserDTO) session.getAttribute("user");
//
//            if (user != null && redisTemplate.opsForValue().get("banned_user:" + user.getId()) != null) {
//                request.getRequestDispatcher("/banned.html").forward(request, response);
//                return;
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}
