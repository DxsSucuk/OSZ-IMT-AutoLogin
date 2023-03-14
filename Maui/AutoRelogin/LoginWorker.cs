using Microsoft.Maui.Networking;
using Mopups.Services;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web;

namespace AutoRelogin
{
    public class LoginWorker
    {
        public LoginWorker() { }

        private static HttpClient sharedClient = new();

        public static async void Login(string username, string password)
        {
            NetworkAccess networkAccess = Connectivity.Current.NetworkAccess;
            
            if (!networkAccess.HasFlag(NetworkAccess.ConstrainedInternet) && !networkAccess.HasFlag(NetworkAccess.Internet))
            {
                return;
            }

            HttpResponseMessage checkResponse = await sharedClient.GetAsync("https://wlan-login.oszimt.de/logon/cgi/index.cgi");
            string responseString = await checkResponse.Content.ReadAsStringAsync();

            if (responseString.Contains("<h3 class=\"headline\">Ticket Anmeldung</h3>"))
            {
                if (responseString.Contains("name=\"ta_id\" value=\""))
                {
                    string currentTAID = responseString.Split("name=\"ta_id\" value=\"")[1].Split("\"")[0];
                    NameValueCollection outgoingQueryString = HttpUtility.ParseQueryString(String.Empty);
                    outgoingQueryString.Add("ta_id", currentTAID);
                    outgoingQueryString.Add("uid", username);
                    outgoingQueryString.Add("pwd", password);
                    outgoingQueryString.Add("device_infos", "1032:1920:1080:1920");
                    outgoingQueryString.Add("voucher_logon_btn", "Login");
                    string postdata = outgoingQueryString.ToString();

                    HttpResponseMessage loginResponse = await sharedClient.PostAsync("https://wlan-login.oszimt.de/logon/cgi/index.cgi",
                        new StringContent(postdata, Encoding.UTF8, "application/x-www-form-urlencoded"));
                    string loginResponseString = await loginResponse.Content.ReadAsStringAsync();

                    if (loginResponseString.Contains("<label class=\"ewc_s_label\"><span class=\"logged-in\">angemeldet</span></label>"))
                    {
                        Debug.WriteLine("Login successful");
                        try
                        {
                            if (MopupService.IsSupported)
                            {
                                await MopupService.Instance.PushAsync(new SuccessPopup()
                                {
                                    Message = "Login success!",
                                });
                            }
                        }
                        catch (Exception ex) { }
                    }
                    else
                    {
                        Debug.WriteLine("Login failed");
                    }
                }
                else
                {
                    Debug.WriteLine("No TA_ID found!");
                }
            }
            else
            {
                try
                {
                    if (MopupService.IsSupported)
                    {
                        await MopupService.Instance.PushAsync(new SuccessPopup()
                        {
                            Message = "Already logged in!",
                        });
                    }
                }
                catch (Exception ex) { }
                Debug.WriteLine("No Login requirement found!");
            }
        }
    }
}
