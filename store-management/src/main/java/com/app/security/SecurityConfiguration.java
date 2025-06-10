package com.app.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.app.entity.Account;
import com.app.service.AccountService;
import com.app.service.AuthorityService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	// private CustomUserDetailsService userDetailService;
	// public SecurityConfiguration(CustomUserDetailsService userDetailService) {
	// this.userDetailService = userDetailService;
	// }

	@Autowired
	AccountService accountService;

	@Autowired
	AuthorityService authorityService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// .csrf().disable()
				// .authorizeHttpRequests()
				// .requestMatchers("/").permitAll()
				// .requestMatchers("/user").hasRole("USER")
				// .requestMatchers("/admin").hasRole("ADMIN")
				// .anyRequest().authenticated()
				// .and()
				// .httpBasic()
				// .and().build();
				// another way
				.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> {
					auth.requestMatchers("/order/**").authenticated();
					auth.requestMatchers("/cart/**").authenticated();
					auth.requestMatchers("/admin/**").hasAnyRole("STAF", "DIRE");
					auth.requestMatchers("/rest/authorities").hasRole("DIRE");
					auth.anyRequest().permitAll();
				})
				// .httpBasic(Customizer.withDefaults()).build();
				.formLogin().loginPage("/login/form")// dia chi url
				.loginProcessingUrl("/login")// action form login
				.defaultSuccessUrl("/", false)// trang sau khi dang nhap thanh cong
				.failureUrl("/login/error")// dia chi url khi co loi
				.usernameParameter("username").passwordParameter("password").and().logout().logoutUrl("/logout")// link
				// th:href="@{|/logout|}"
				.logoutSuccessUrl("/logout/success")// logout xong di dau
				.and().exceptionHandling().accessDeniedPage("/unauthoried");// khong co quyen vao trang do
		return http.build();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsManager() {
		List<Account> list = accountService.findAll();
		Collection<UserDetails> users = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			List<String> roles = authorityService.findRoles(list.get(i).getUsername());
			String[] r = new String[roles.size()];
			roles.toArray(r);

			UserDetails user = User.withUsername(list.get(i).getUsername())
					.password(passwordEncoder().encode(list.get(i).getPassword()))
					.roles(r).build();
			users.add(user);
		}
		return new InMemoryUserDetailsManager(users);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
