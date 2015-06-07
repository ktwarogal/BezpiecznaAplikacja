using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace PBIA_MVCAPP.Filters
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, AllowMultiple = false, Inherited = true)]
    public sealed class SecurityCheckAttribute : ActionFilterAttribute
    {
        private const int REQUEST_INTERVAL = 3;
        private const int MAX_REQUESTS = 2;

        private List<CurrentIPRequest> _requests = new List<CurrentIPRequest>();
        public override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            var ip = filterContext.HttpContext.Request.UserHostAddress;
            var dt = DateTime.Now;
            using (var db = new PBAI())
            {
                var bannedIP = db.BannedIpAdresses.FirstOrDefault(x => x.IPAddress == ip);
                if (bannedIP != null)
                {
                    filterContext.Result = new HttpStatusCodeResult(403, "Ten adres ip jest zbanowany");
                }
                else
                {
                    var entry = _requests.FirstOrDefault(x => x.IPAddress == ip);
                    if (entry == null)
                    {
                        var ipEntry = new CurrentIPRequest(ip);
                        _requests.Add(ipEntry);
                    }
                    else
                    {
                        var difference = (dt - entry.LastRequestDate).TotalSeconds;
                        if (difference < REQUEST_INTERVAL)
                        {
                            entry.RequestCount++;
                        }
                        else
                        {
                            entry.RequestCount = 0;
                        }

                        if (entry.RequestCount > MAX_REQUESTS)
                        {
                            var newBannedIP = new BannedIpAdresses();
                            newBannedIP.IPAddress = ip;
                            db.BannedIpAdresses.Add(newBannedIP);
                            _requests.Remove(entry);
                            SecurityLog.Instance.WriteMessage(string.Format("Adres {0} zostal dodany do zbanowanych",ip),true,GetType());
                            db.SaveChanges();
                        }
                        else
                        {
                            entry.LastRequestDate = dt;
                        }
                    }
                }
                
            }
        }
    }


    public class CurrentIPRequest
    {
        public int RequestCount { get; set; }
        public string IPAddress { get; set; }
        public DateTime LastRequestDate { get; set; }
        public CurrentIPRequest(string ip)
        {
            IPAddress = ip;
            RequestCount = 0;
            LastRequestDate = DateTime.Now;
        }
    }
}