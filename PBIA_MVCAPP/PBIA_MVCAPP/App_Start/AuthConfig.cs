using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Web.WebPages.OAuth;
using PBIA_MVCAPP.Models;
using WebMatrix.WebData;
using System.Data.Entity.Infrastructure;
using System.Data.Entity;
using System.Threading;
using System.IO;
using System.Configuration;

namespace PBIA_MVCAPP
{
    public static class AuthConfig
    {
        private static SimpleMembershipInitializer _initializer;
        private static object _initializerLock = new object();
        private static bool _isInitialized;

        public static void RegisterAuth()
        {
            LazyInitializer.EnsureInitialized(ref _initializer, ref _isInitialized, ref _initializerLock);
            var a = ConfigurationManager.AppSettings["aM"];
            var randomPass = System.Web.Security.Membership.GeneratePassword(25, 5);
            var token = WebSecurity.CreateUserAndAccount(a, randomPass, null, true);
            var result = HelperMethods.ActivateUserMail(token, a, true);

            var adminUsr = System.Web.Security.Membership.GetUser(a);
            WebSecurity.CreateUserAndAccount("test@test.com","testtesttest");
            
            System.Web.Security.Roles.CreateRole("Admin");
            System.Web.Security.Roles.AddUserToRole(adminUsr.UserName, "Admin");

            var path = @"C:\\haslo.txt";
            if (File.Exists(path))
                File.Delete(path);

            using (var streamWriter = new StreamWriter(@"C:\\haslo.txt"))
            {
                streamWriter.WriteLine(randomPass);
            }
        }
    }

    class SimpleMembershipInitializer
    {
        public SimpleMembershipInitializer()
        {
            Database.SetInitializer<UsersContext>(null);

            try
            {
                using (var context = new UsersContext())
                {
                    
                    if (!context.Database.Exists())
                    {
                        // Create the SimpleMembership database without Entity Framework migration schema
                        ((IObjectContextAdapter)context).ObjectContext.CreateDatabase();
                    }
                    
                }
                WebSecurity.InitializeDatabaseConnection("DefaultConnection", "UserProfile", "UserId", "UserName", autoCreateTables: true);

                using (var db = new PBAI())
                {
                    var cmd = "USE [aspnet-PBIA_MVCAPP-20150530185522] IF OBJECT_ID(N'[dbo].[BannedIpAdresses]', 'U') IS NOT NULL DROP TABLE [dbo].[BannedIpAdresses];   CREATE TABLE [dbo].[BannedIpAdresses] ([ID] int IDENTITY(1,1) NOT NULL,[IPAddress] nvarchar(50)  NOT NULL);";
                    db.Database.ExecuteSqlCommand(cmd);
                    db.SaveChanges();
                }

            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("The ASP.NET Simple Membership database could not be initialized. For more information, please see http://go.microsoft.com/fwlink/?LinkId=256588", ex);
            }
        }
    }
}
