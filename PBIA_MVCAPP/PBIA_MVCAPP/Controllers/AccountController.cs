using System;
using System.Collections.Generic;
using System.Linq;
using System.Transactions;
using System.Web;
using System.Web.Mvc;
using System.Web.Security;
using DotNetOpenAuth.AspNet;
using Microsoft.Web.WebPages.OAuth;
using WebMatrix.WebData;
using PBIA_MVCAPP.Filters;
using PBIA_MVCAPP.Models;
using System.Net;
using HtmlAgilityPack;

namespace PBIA_MVCAPP.Controllers
{
    [RequireHttps]
    [Authorize]
    [SecurityCheck]
    public class AccountController : Controller
    {
        private const string PAGE_URL = "http://technologieinter.esy.es/strazmiejska.html";
        private const int MAX_UNSUCESSFULL_LOGINS = 2;

        //
        // GET: /Account/Login

        [AllowAnonymous]
        public ActionResult Login(string returnUrl)
        {
            ViewBag.ReturnUrl = returnUrl;
            return View();
        }

        //
        // POST: /Account/Login

        [HttpPost]
        [AllowAnonymous]
        [ValidateAntiForgeryToken]
        public ActionResult Login(LoginModelMVC model, string returnUrl)
        {
            if (WebSecurity.UserExists(model.UserName))
            {
                var isConfirmed = WebSecurity.IsConfirmed(model.UserName);
                if (isConfirmed)
                {
                    if (ModelState.IsValid)
                    {
                        var usr = System.Web.Security.Membership.GetUser(model.UserName);
                        if (usr.IsApproved)
                        {
                            var isLoggedIn = WebSecurity.Login(model.UserName, model.Password, persistCookie: model.RememberMe);

                            if (isLoggedIn)
                                return RedirectToLocal(returnUrl);
                            else
                            {
                                ModelState.AddModelError("", "Logowanie nie powiodło się");
                                SecurityLog.Instance.WriteMessage(string.Format("Nieudane logowanie dla {0} - zle dane logowania", model.UserName), false, GetType());
                                if (CheckForBan(model.UserName, HttpContext.Request.UserHostAddress))
                                {
                                    return RedirectToAction("UserBanned");
                                }
                            }
                        }
                        else
                        {
                            ModelState.AddModelError("", "Ten użytkownik jest zbanowany. Wymagana ręczna aktywacja przez administratora");
                            SecurityLog.Instance.WriteMessage(string.Format("Nieudane logowanie dla {0} - użytkownik zbanowany. Wymagana ręczna aktywacja przed administratora", model.UserName), false, GetType());
                        }
                    }
                    else
                    {
                        ModelState.AddModelError("", "Nieprawidłowe dane logowania");
                        SecurityLog.Instance.WriteMessage(string.Format("Nieudane logowanie dla {0} - zle wpisane dane", model.UserName), false, GetType());
                    }
                }
                else
                {
                    SecurityLog.Instance.WriteMessage(string.Format("Nieudane logowanie dla {0} - nieaktywny", model.UserName), false, GetType());
                    ModelState.AddModelError("", "To konto nie zostało jeszcze aktywowane");
                }
            }
            else
            {
                SecurityLog.Instance.WriteMessage(string.Format("Nieudane logowanie dla {0} - nie istnieje", model.UserName), false, GetType());
                ModelState.AddModelError("", "To konto nie istnieje");
            }

            
            return View(model);
        }



        private bool CheckForBan(string l, string ip)
        {
            using (var db = new PBAI())
            {
                var up = db.UserProfile.FirstOrDefault(x => x.UserName == l).UserId;
                var mu = db.webpages_Membership.FirstOrDefault(x=>x.UserId == up);
                if (mu.PasswordFailuresSinceLastSuccess > MAX_UNSUCESSFULL_LOGINS)
                {
                    mu.PasswordFailuresSinceLastSuccess = 0;
                    mu.IsConfirmed = false;
                    var token = string.Concat(Guid.NewGuid().ToString().Take(128)).Replace("=",string.Empty).Replace("-",string.Empty);
                    mu.ConfirmationToken = token;
                    HelperMethods.ActivateUserByAdminEmail(token, l);
                    HelperMethods.BanIpAddress(ip);
                    db.SaveChanges();
                    return true;
                }
            }
            return false;
        }



        [HttpGet]
        [Authorize(Roles = "Admin")]
        public ActionResult UnbanUser(string token)
        {
            try
            {
                using (var db = new PBAI())
                {
                    var mu = db.webpages_Membership.FirstOrDefault(x => x.ConfirmationToken == token);
                    mu.IsConfirmed = true;
                    db.SaveChanges();
                }
                ViewBag.Result = "powiodło się";
            }
            catch (Exception ex)
            {
                ViewBag.Result = "nie powiodło się";
            }
            return View();
        }


        [HttpGet]
        [AllowAnonymous]
        public string LoginViaMobile(string l, string p)
        {
            if (l == null || p == null)
                return null;
            var provider = (SimpleMembershipProvider)System.Web.Security.Membership.Provider;
            var result = provider.ValidateUser(l, p);

            var usr = provider.GetUser(l, false);
            if (usr == null)
            {
                var msg = string.Format("Proba zalogowania na niestniejacego uzytkownika {0}", l);
                SecurityLog.Instance.WriteMessage(msg, false, GetType());
                return null;
            }

            if (!result)
            {
                var msg = string.Format("Nieudane logowanie przez e dla uzytkownika {0}", l);
                SecurityLog.Instance.WriteMessage(msg, false, GetType());
                return null;
            }
            else
            {
                var msg = string.Format("Zalogowano przez komorke {0}", l);
                SecurityLog.Instance.WriteMessage(msg, true, GetType());
                return GetSpeedCameras();
            }
        }

        [HttpGet]
        [AllowAnonymous]
        public string GetVersion()
        {
            return "1.0";
        }

        [HttpGet]
        [AllowAnonymous]
        public ActionResult ActivateUser(string token)
        {
            var isSuccess = WebSecurity.ConfirmAccount(token);
            if(isSuccess)
                return RedirectToAction("UserActivated", "Account");
            else
                return RedirectToAction("UserActivationFailed", "Account");
        }

        [AllowAnonymous]
        public ActionResult UserActivated()
        {
            return View();
        }

        [AllowAnonymous]
        public ActionResult UserActivationFailed()
        {
            return View();
        }

        [AllowAnonymous]
        public ActionResult Registered()
        {
            return View();
        }

        private string GetSpeedCameras()
        {
            var speedCameras = string.Empty;

            try
            {
                using (var wc = new WebClient())
                {
                    var html = wc.DownloadString(PAGE_URL);
                    var htmlDoc = new HtmlDocument();
                    htmlDoc.LoadHtml(html);
                    var ulElement = htmlDoc.DocumentNode.Descendants("ul").FirstOrDefault(x => x.Id == "suszing-list");
                    if (ulElement == null)
                    {
                        SecurityLog.Instance.WriteMessage("Nie mozna znalezc elementu ul na stronie", true, this.GetType());
                        return null;
                    }

                    var li = ulElement.Descendants("li").ToList();
                    foreach (var listElement in li)
                    {
                        speedCameras+=listElement.InnerText.Trim() + "|";
                    }
                }
            }
            catch (Exception ex)
            {
                SecurityLog.Instance.WriteMessage(string.Format("{0} : {1}", ex.GetType(), ex.Message), true, this.GetType());
                return null;
            }

            SecurityLog.Instance.WriteMessage("asd", true, this.GetType());
            if (string.IsNullOrEmpty(speedCameras))
                return null;
            else
                return speedCameras.Substring(0,speedCameras.Length-1);
        }


        [HttpGet]
        [AllowAnonymous]
        public ActionResult UserBanned()
        {
            return View();
        }

        //
        // POST: /Account/LogOff

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult LogOff()
        {
            WebSecurity.Logout();

            return RedirectToAction("Index", "Home");
        }

        //
        // GET: /Account/Register

        [AllowAnonymous]
        public ActionResult Register()
        {
            return View();
        }

        //
        // POST: /Account/Register

        [HttpPost]
        [AllowAnonymous]
        [ValidateAntiForgeryToken]
        public ActionResult Register(RegisterModelMVC model)
        {
            if (ModelState.IsValid)
            {
                // Attempt to register the user
                try
                {
                    var token = WebSecurity.CreateUserAndAccount(model.UserName, model.Password,null,true);
                    var result = HelperMethods.ActivateUserMail(token, model.UserName);
                    if (result)
                    {
                        SecurityLog.Instance.WriteMessage(string.Format("Zarejestrowano nowego uzytkownika {0}", model.UserName), true, GetType());
                        return RedirectToAction("Registered", "Account");
                    }
                    else
                    {
                        ((SimpleMembershipProvider)Membership.Provider).DeleteAccount(model.UserName); // deletes record from webpages_Membership table
                        ((SimpleMembershipProvider)Membership.Provider).DeleteUser(model.UserName, true); // deletes record from UserProfile table
                        SecurityLog.Instance.WriteMessage(string.Format("Wystąpił błąd podczas wysyłania e-mail do uzytkownika {0}", model.UserName), true, GetType());
                    }
                }
                catch (MembershipCreateUserException e)
                {
                    ModelState.AddModelError("", ErrorCodeToString(e.StatusCode));
                }
            }

            var msg = string.Format("Niepoprawna próba rejestracji z adresu {0}",this.HttpContext.Request.UserHostAddress);
            SecurityLog.Instance.WriteMessage(msg,false,GetType());
            // If we got this far, something failed, redisplay form
            return View(model);
        }

       

        //
        // GET: /Account/Manage

        public ActionResult Manage(ManageMessageId? message)
        {
            ViewBag.StatusMessage =
                message == ManageMessageId.ChangePasswordSuccess ? "Hasło zmienione."
                : message == ManageMessageId.SetPasswordSuccess ? "Hasło ustawione."
                : message == ManageMessageId.RemoveLoginSuccess ? "Wylogowano."
                : "";
            ViewBag.HasLocalPassword = OAuthWebSecurity.HasLocalAccount(WebSecurity.GetUserId(User.Identity.Name));
            ViewBag.ReturnUrl = Url.Action("Manage");
            return View();
        }

        //
        // POST: /Account/Manage

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Manage(LocalPasswordModelMVC model)
        {
            bool hasLocalAccount = OAuthWebSecurity.HasLocalAccount(WebSecurity.GetUserId(User.Identity.Name));
            ViewBag.HasLocalPassword = hasLocalAccount;
            ViewBag.ReturnUrl = Url.Action("Manage");
            if (hasLocalAccount)
            {
                if (ModelState.IsValid)
                {
                    // ChangePassword will throw an exception rather than return false in certain failure scenarios.
                    bool changePasswordSucceeded;
                    try
                    {
                        changePasswordSucceeded = WebSecurity.ChangePassword(User.Identity.Name, model.OldPassword, model.NewPassword);
                    }
                    catch (Exception)
                    {
                        changePasswordSucceeded = false;
                    }

                    if (changePasswordSucceeded)
                    {
                        return RedirectToAction("Manage", new { Message = ManageMessageId.ChangePasswordSuccess });
                    }
                    else
                    {
                        ModelState.AddModelError("", "Któreś z haseł jest nieprawidłowe");
                    }
                }
            }
            else
            {
                // User does not have a local password so remove any validation errors caused by a missing
                // OldPassword field
                ModelState state = ModelState["OldPassword"];
                if (state != null)
                {
                    state.Errors.Clear();
                }

                if (ModelState.IsValid)
                {
                    try
                    {
                        WebSecurity.CreateAccount(User.Identity.Name, model.NewPassword);
                        return RedirectToAction("Manage", new { Message = ManageMessageId.SetPasswordSuccess });
                    }
                    catch (Exception)
                    {
                        ModelState.AddModelError("", String.Format("Konto o takiej nazwie już istnieje."));
                    }
                }
            }

            // If we got this far, something failed, redisplay form
            return View(model);
        }

        //
        // POST: /Account/ExternalLogin

        

        #region Helpers
        private ActionResult RedirectToLocal(string returnUrl)
        {
            if (Url.IsLocalUrl(returnUrl))
            {
                return Redirect(returnUrl);
            }
            else
            {
                return RedirectToAction("Index", "Home");
            }
        }

        public enum ManageMessageId
        {
            ChangePasswordSuccess,
            SetPasswordSuccess,
            RemoveLoginSuccess,
        }

        internal class ExternalLoginResult : ActionResult
        {
            public ExternalLoginResult(string provider, string returnUrl)
            {
                Provider = provider;
                ReturnUrl = returnUrl;
            }

            public string Provider { get; private set; }
            public string ReturnUrl { get; private set; }

            public override void ExecuteResult(ControllerContext context)
            {
                OAuthWebSecurity.RequestAuthentication(Provider, ReturnUrl);
            }
        }

        private static string ErrorCodeToString(MembershipCreateStatus createStatus)
        {
            // See http://go.microsoft.com/fwlink/?LinkID=177550 for
            // a full list of status codes.
            switch (createStatus)
            {
                case MembershipCreateStatus.DuplicateUserName:
                    return "Taki użytkownik już istnieje";

                case MembershipCreateStatus.DuplicateEmail:
                    return "Ten adres e-mail został już użyty";

                case MembershipCreateStatus.InvalidPassword:
                    return "To hasło jest nieprawidłowe";

                case MembershipCreateStatus.InvalidEmail:
                    return "Ten e-mail jest nieprawidłowy";

                case MembershipCreateStatus.InvalidAnswer:
                    return "Ta odpowiedź do pytania jest nieprawidłowa";

                case MembershipCreateStatus.InvalidQuestion:
                    return "To pytanie jest nieprawidłowe";

                case MembershipCreateStatus.InvalidUserName:
                    return "Ta nazwa użytkownika jest nieprawidłowa";

                case MembershipCreateStatus.ProviderError:
                    return "Wystąpił błąd wewnętrzny systemu. Skontaktuj się z administratorem";

                case MembershipCreateStatus.UserRejected:
                    return "Utworzenie konta zostało zablokowane. Skontaktuj się z administratorem";

                default:
                    return "Wystąpił nieznany błąd. Skontaktuj się z administratorem";
            }
        }
        #endregion
    }
}
