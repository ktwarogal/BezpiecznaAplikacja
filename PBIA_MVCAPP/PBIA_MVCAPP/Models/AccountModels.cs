﻿using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Data.Entity;
using System.Globalization;
using System.Web.Security;

namespace PBIA_MVCAPP.Models
{
    public class UsersContext : DbContext
    {
        public UsersContext()
            : base("DefaultConnection")
        {
        }

        public DbSet<UserProfileMVC> UserProfiles { get; set; }
    }

    [Table("UserProfile")]
    public class UserProfileMVC
    {
        [Key]
        [DatabaseGeneratedAttribute(DatabaseGeneratedOption.Identity)]
        public int UserId { get; set; }
        public string UserName { get; set; }
    }

    public class LocalPasswordModelMVC
    {
        [Required]
        [DataType(DataType.Password)]
        [Display(Name = "Aktualne hasło")]
        public string OldPassword { get; set; }

        [Required]
        [StringLength(100, ErrorMessage = "{0} musi mieć minimum {2} znaków.", MinimumLength = 6)]
        [DataType(DataType.Password)]
        [Display(Name = "Nowe hasło")]
        public string NewPassword { get; set; }

        [DataType(DataType.Password)]
        [Display(Name = "Potwierdź nowe hasło")]
        [Compare("NewPassword", ErrorMessage = "Hasła w obu polach nie są zgodne.")]
        public string ConfirmPassword { get; set; }
    }

    public class LoginModelMVC
    {
        [Required]
        [Display(Name = "E-mail")]
        public string UserName { get; set; }

        [Required]
        [DataType(DataType.Password)]
        [Display(Name = "Hasło")]
        public string Password { get; set; }

        [Display(Name = "Zapamiętać?")]
        public bool RememberMe { get; set; }
    }

    public class RegisterModelMVC
    {
        [Required]
        [EmailAddress]
        [Display(Name = "E-mail (podaj prawdziwy)")]
        public string UserName { get; set; }

        [Required]
        [StringLength(100, ErrorMessage = "{0} musi mieć minimum {2} znaków.", MinimumLength = 8)]
        [RegularExpression(@"^(?=.*\d)(?=.*\d)(?=.*[!@#$%])(?=.*[a-zA-Z]).{8,100}$", ErrorMessage = "Wymagana minimum jedna cyfra i znak specjalny")]
        [DataType(DataType.Password)]
        [Display(Name = "Hasło")]
        public string Password { get; set; }

        [RegularExpression(@"^(?=.*\d)(?=.*\d)(?=.*[!@#$%])(?=.*[a-zA-Z]).{8,100}$", ErrorMessage = "Wymagana minimum jedna cyfra i znak specjalny")]
        [DataType(DataType.Password)]
        [Display(Name = "Potwierdź hasło")]
        [Compare("Password", ErrorMessage = "Hasła w obu polach nie są zgodne.")]
        public string ConfirmPassword { get; set; }
    }


    public class ResetModelMVC1
    {
        [Required]
        [EmailAddress]
        [Display(Name = "E-mail (podaj prawdziwy)")]
        public string UserName { get; set; }
    }


    public class ResetModelMVC2
    {
        [Required]
        [StringLength(100, ErrorMessage = "{0} musi mieć minimum {2} znaków.", MinimumLength = 8)]
        [DataType(DataType.Password)]
        [Display(Name = "Hasło")]
        public string Password { get; set; }

        [Required]
        [DataType(DataType.Password)]
        [Display(Name = "Potwierdź hasło")]
        [Compare("Password", ErrorMessage = "Hasła w obu polach nie są zgodne.")]
        public string ConfirmPassword { get; set; }

        public string Token{ get; set; }
    }

}
