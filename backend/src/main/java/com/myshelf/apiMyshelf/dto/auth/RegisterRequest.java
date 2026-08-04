package com.myshelf.apiMyshelf.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "L'adresse email est obligatoire.")
    @Email(message = "L'adresse email est invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Le mot de passe doit contenir une majuscule, une minuscule et un chiffre."
    )
    private String password;

    @NotBlank(message = "Le nom d'affichage est obligatoire.")
    @Size(max = 20, message = "Le nom d'affichage ne peut pas dépasser 20 caractères.")
    private String displayName;
}
