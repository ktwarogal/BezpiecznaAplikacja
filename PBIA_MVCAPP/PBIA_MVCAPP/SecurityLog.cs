using log4net;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace PBIA_MVCAPP
{
    public class SecurityLog
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(SecurityLog));
        private static SecurityLog _instance;
        public static SecurityLog Instance
        {
            get
            {
                if (_instance == null)
                {
                    log4net.Config.XmlConfigurator.Configure();
                    _instance = new SecurityLog();
                }
                return _instance;
            }
        }

        public void WriteMessage(string str,bool isCorrect, Type t)
        {
            if (isCorrect)
            {
                str = t+ "->" + str.Trim() + "-> OK";
            }
            else
            {
                str = t+ "->" + str.Trim() + "-> FAIL";
            }
            Log.Info(str);
        }
    }
}