package com.marcedev.stock;

import com.marcedev.stock.entity.Role;
import com.marcedev.stock.entity.User;
import com.marcedev.stock.repository.RoleRepository;
import com.marcedev.stock.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class StockApplication {

	public static void main(String[] args) {


		SpringApplication.run(StockApplication.class, args);

		System.out.println(new BCryptPasswordEncoder().encode("admin123"));

	}

}
