package br.pedro.rproject.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class TokenVerifyFilter extends BasicAuthenticationFilter {

    public TokenVerifyFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String att = request.getHeader("Authorization");

        if (att == null) {
            chain.doFilter(request, response);
            return;
        }

        if (!att.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = att.replace("Bearer ", "");
        UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(token);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(String token) {
        try {
            DecodedJWT tokenDecoded = JWT.require(Algorithm.HMAC512("SECRET"))
                    .build()
                    .verify(token);

            return new UsernamePasswordAuthenticationToken(tokenDecoded.getSubject(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(tokenDecoded.getClaim("role").asString())));

        } catch (JWTVerificationException ex){
            return null;
        }
    }
}
