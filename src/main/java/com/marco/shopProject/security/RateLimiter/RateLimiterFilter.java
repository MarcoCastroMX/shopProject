package com.marco.shopProject.security.RateLimiter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private Bucket createNewBucket(){
        Bandwidth limit = Bandwidth.classic(10,Refill.greedy(10,Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String xff = request.getHeader("X-Forwarded-For");
        String key = (xff != null) ? xff.split(",")[0] : request.getRemoteAddr();

        Bucket bucket = cache.get(key, k -> createNewBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(probe.isConsumed()){
            response.addHeader("X-Rate-Limit-Remaining",String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request,response);
        }else{
            response.setStatus(429);
            long waitForRefill = probe.getNanosToWaitForRefill();
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.getWriter().write("Has superados el limite. Reintenta en " + waitForRefill + " segundos.");
        }
    }
}
