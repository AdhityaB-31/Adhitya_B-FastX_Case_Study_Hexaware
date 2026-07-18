package com.hexaware.fastx.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hexaware.fastx.filter.JwtAuthFilter;
import com.hexaware.fastx.security.FastXUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize on controller methods
public class SecurityConfig {

	@Autowired
	private JwtAuthFilter jwtAuthFilter;

	@Autowired
	private FastXUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http

				.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/api/auth/register/user", // register as passenger
								"/api/auth/register/operator", // register as bus operator
								"/api/auth/login", // login (all roles)
								"/api/auth/forgot-password", // request password reset email
								"/api/auth/reset-password", // reset password with token
								"/api/auth/google", // Google Sign-In
								"/api/buses/all", // get all buses (public)
								"/api/buses/search", // search buses (public)
								"/api/buses/type", // filter by type (public)
								"/api/buses/fare", // filter by fare (public)
								"/api/buses/cheapest", // cheapest buses (public)
								"/v3/api-docs/**", // Swagger JSON docs
								"/swagger-ui/**", // Swagger UI assets
								"/swagger-ui.html")
						.permitAll()

						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/roles/**")
						.hasRole("ADMIN")

						.requestMatchers("/api/users").hasRole("ADMIN").requestMatchers("/api/users/active")
						.hasRole("ADMIN").requestMatchers("/api/users/search").hasRole("ADMIN")
						.requestMatchers("/api/users/delete/**").hasRole("ADMIN")

						.requestMatchers("/api/operators").hasRole("ADMIN").requestMatchers("/api/operators/active")
						.hasRole("ADMIN").requestMatchers("/api/operators/search").hasRole("ADMIN")
						.requestMatchers("/api/operators/delete/**").hasRole("ADMIN")

						.requestMatchers("/api/refunds/*/approve", "/api/refunds/*/reject", "/api/refunds/operator/**").hasRole("BUS_OPERATOR")
						.requestMatchers("/api/refunds/status", "/api/refunds/getall").hasRole("ADMIN")
						.requestMatchers("/api/payments/*/refund").hasRole("ADMIN")

						.requestMatchers("/api/buses/create", // add new bus
								"/api/buses/update/**", // update bus details
								"/api/buses/delete/**", // remove bus
								"/api/buses/operator/**", // list buses by operator
								"/api/buses/search/name", // search buses by name
								"/api/seats/release", // release seats back to available
								"/api/operators/**" // view/update own operator profile
						).hasAnyRole("BUS_OPERATOR", "ADMIN")

						.requestMatchers("/api/bookings/create", // create a new booking
								"/api/bookings/*/confirm", // confirm (post-payment)
								"/api/bookings/*/cancel", // cancel own booking
								"/api/bookings/user/**", // view own bookings list
								"/api/bookings/fare", // calculate fare
								"/api/payments/process/**", // make a payment
								"/api/razorpay/create-order", // initiate Razorpay order
								"/api/razorpay/verify-payment", // verify Razorpay signature
								"/api/payments/booking/**", // view payment for a booking
								"/api/payments/*/verify", // verify payment status
								"/api/users/*/bookings", // view own booking history
								"/api/users/update/**", // update own profile
								"/api/users/email/**", // look up user by email
								"/api/users/**" // view user profile
						).hasAnyRole("USER", "ADMIN")

						.requestMatchers("/api/buses/**", // bus details & available seats
								"/api/seats/**", // view seat info
								"/api/amenities/**", // amenity info
								"/api/bookings/**", // view a single booking / fare
								"/api/refunds/booking/**", // request a refund
								"/api/refunds/**" // view refund status
						).authenticated()

						.anyRequest().authenticated())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
				List.of("http://localhost:5173", "http://127.0.0.1:5173", "http://16.112.244.120",
						"http://16.112.244.120.nip.io", "http://localhost", "http://fastx.16.112.244.120.nip.io"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		configuration.setAllowedHeaders(List.of("*"));

		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
