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
        public const string MSG_TITLE = "Aktywacja użytkownika";
        public const string MSG_TITLE2 = "Ponowna aktywacja użytkownika";
        public static bool ActivateUserMail(string token, string inactiveUsername, bool isAdmin=false)
        {
            var fullUrl = string.Format("https://projekt-pbai.pl/PBAI_WebApp/Account/ActivateUser?token={0}",token);
            var fullText = string.Empty;
            if(!isAdmin)
                fullText = string.Format("Link do aktywacji konta: {0}", fullUrl);
            else
                fullText = string.Format("Link do aktywacji konta administratora: {0}. Haslo zostało zapisane na pulpicie serwera.", fullUrl);

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
                    Credentials = new NetworkCredential(l,p),
                    Timeout = 20000
                };

                using (var message = new MailMessage(l, inactiveUsername)
                {
                    Subject = HelperMethods.MSG_TITLE,
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

        public static void BanIpAddress(string ip)
        {
            using (var db = new PBAI())
            {
                var newBannedIP = new BannedIpAdresses();
                newBannedIP.IPAddress = ip;
                db.BannedIpAdresses.Add(newBannedIP);
                SecurityLog.Instance.WriteMessage(string.Format("Adres {0} zostal dodany do zbanowanych", ip), true, typeof(HelperMethods));
                db.SaveChanges();
            }
        }


        public static bool ActivateUserByAdminEmail(string token, string inactiveUsername)
        {
            var fullUrl = string.Format("https://projekt-pbai.pl/PBAI_WebApp/Account/UnbanUser?token={0}", token);
            var fullText = string.Format("Konto {0} musi zostać ponownie aktywowane z powodu zbyt dużej ilości niepoprawnych logowań . Link: {1}", inactiveUsername, fullUrl);


            SecurityLog.Instance.WriteMessage("Generowanie linku aktywacyjnego dla " + inactiveUsername, true, typeof(HelperMethods));
            try
            {
                var l = ConfigurationManager.AppSettings["eSvcL"];
                var p = ConfigurationManager.AppSettings["eSvcP"];
                var a = ConfigurationManager.AppSettings["aM"];
                var smtp = new SmtpClient
                {
                    Host = "smtp.gmail.com",
                    Port = 587,
                    EnableSsl = true,
                    DeliveryMethod = SmtpDeliveryMethod.Network,
                    Credentials = new NetworkCredential(l, p),
                    Timeout = 20000
                };

                using (var message = new MailMessage(l, a)
                {
                    Subject = MSG_TITLE2,
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
    }
}