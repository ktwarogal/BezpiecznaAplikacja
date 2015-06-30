using PBIA_MVCAPP.Filters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace PBIA_MVCAPP.Controllers
{
    [SecurityCheck]
    [RequireHttps]
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            ViewBag.Message = "Strona domowa";
            return View();
        }

        public ActionResult About()
        {
            ViewBag.Message = "O nas";

            return View();
        }

        public ActionResult Contact()
        {
            ViewBag.Message = "Kontakt";
            return View();
        }
    }
}
