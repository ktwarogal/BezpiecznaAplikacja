using PBIA_MVCAPP.Filters;
using System.Web;
using System.Web.Mvc;

namespace PBIA_MVCAPP
{
    public class FilterConfig
    {
        public static void RegisterGlobalFilters(GlobalFilterCollection filters)
        {
            filters.Add(new HandleErrorAttribute());
            filters.Add(new SecurityCheckAttribute());
            filters.Add(new ApplicationStabilityAttribute());
        }
    }
}