using PBIA_MVCAPP.Filters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Security.Cryptography;
using System.ServiceModel;
using System.ServiceModel.Activation;
using System.Text;
using WebMatrix.WebData;

namespace PBIA_MVCAPP
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "Service1" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select Service1.svc or Service1.svc.cs at the Solution Explorer and start debugging.
    [AspNetCompatibilityRequirements(
        RequirementsMode = AspNetCompatibilityRequirementsMode.Allowed)]
    [InitializeSimpleMembership]
    public class PBAI_WebService : IPBAI_WebService
    {
        public List<string> GetSpeedCameras()
        {
            var speedCameras = new List<string>();
            speedCameras.Add("53.425035, 14.550363"); //Brama Portowa
            speedCameras.Add("53.427023, 14.536748"); //Plac Kościuszki
            speedCameras.Add("53.428589, 14.512586"); //26 Kwietnia i Santocka
            speedCameras.Add("53.413008, 14.523669"); //Mieszka I
            speedCameras.Add("53.460952, 14.498034"); //al. Wojska Polskiego
            speedCameras.Add("53.488916, 14.481691"); //rzeczka Pilchówka
            speedCameras.Add("53.448046, 14.518629"); //al. Wojska Polskiego 2
            speedCameras.Add("53.442991, 14.520743"); //B. Lindego
            speedCameras.Add("53.434240, 14.518817"); //Witkiewicza
            speedCameras.Add("53.436473, 14.491198"); //Taczaka
            return speedCameras;
        }

        public bool LogIn(string login, string pass)
        {
            var provider = (SimpleMembershipProvider)System.Web.Security.Membership.Provider;
            var result = provider.ValidateUser(login, pass);
            return result;
        }
    }
}
