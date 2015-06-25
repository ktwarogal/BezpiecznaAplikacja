using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Mail;
using System.Web;
using System.Security.Cryptography;
using System.Text;
using System.Configuration;

namespace PBIA_MVCAPP
{
    public class HelperMethods
    {
        public const string REPORTING_SERVICE_EMAIL_TITLE = "Aktywacja użytkownika";
        public static bool ActivateUserMail(string token, string inactiveUsername)
        {
            var fullUrl = string.Format("https://projekt-pbai.pl/PBAI_WebApp/Account/ActivateUser?token={0}",token);

            var fullText = string.Format("Link do aktywacji konta: {0}", fullUrl);
            SecurityLog.Instance.WriteMessage("Generowanie linku aktywacyjnego dla " + inactiveUsername,true, SecurityLog.Instance.GetType());
            try
            {
                var l = ConfigurationManager.AppSettings["eSvcL"];
                var p = ConfigurationManager.AppSettings["eSvcP"];
                var smtp = new SmtpClient
                {
                    Host = "smtp.gmail.com",
                    Port = 587,
                    EnableSsl = true,
                    DeliveryMethod = SmtpDeliveryMethod.Network,
                    Credentials = new NetworkCredential(l,p ),
                    Timeout = 20000
                };

                using (var message = new MailMessage(l, inactiveUsername)
                {
                    Subject = HelperMethods.REPORTING_SERVICE_EMAIL_TITLE,
                    Body = fullText,
                    SubjectEncoding = System.Text.Encoding.UTF8,
                    BodyEncoding = System.Text.Encoding.UTF8
                })
                {
                    smtp.Send(message);
                }
                return true;
            }
            catch (Exception ex)
            {
                SecurityLog.Instance.WriteMessage(ex.Message, false, SecurityLog.Instance.GetType());
            }
            return false;
        }

        public static string GenerateLoginString(string login)
        {
            var guid = new Guid().ToString();
            using (var db = new PBAI())
            {
                var inactiveUser = new InactiveUserLink();
                inactiveUser.MagicGuid = guid;
                inactiveUser.UserName = login;
                db.InactiveUserLinks.Add(inactiveUser);
                db.SaveChanges();
            }
            return guid;
        }

        public static void RemoveActivationString(string login)
        {
            using (var db = new PBAI())
            {
                var itemToDelete = db.InactiveUserLinks.FirstOrDefault(x => x.UserName == login);
                if (itemToDelete != null)
                    db.InactiveUserLinks.Remove(itemToDelete);

                db.SaveChanges();
            }
        }
    }
}