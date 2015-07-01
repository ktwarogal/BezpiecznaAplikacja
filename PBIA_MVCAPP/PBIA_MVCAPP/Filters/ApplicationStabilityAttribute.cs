using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Web.Routing;

namespace PBIA_MVCAPP.Filters
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, AllowMultiple = false, Inherited = true)]
    public class ApplicationStabilityAttribute : ActionFilterAttribute
    {
        public override void OnActionExecuting(ActionExecutingContext filterContext)
        {   
            if(HelperMethods.FATAL_ERROR_OCCURRED)
            {
                filterContext.Result = new HttpStatusCodeResult(500, "Blad. Aplikacja wymaga interwencji administratora.");
            }
        }
    }
}