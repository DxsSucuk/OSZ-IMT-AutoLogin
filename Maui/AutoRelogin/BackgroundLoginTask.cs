namespace AutoRelogin
{
    public class BackgroundLoginTask
    {
        private Task? task;
        private string Username { get; set; }
        private string Password { get; set; }
        private readonly PeriodicTimer periodicTimer;
        private readonly CancellationTokenSource cancellationSource = new();

        public BackgroundLoginTask(TimeSpan timespan, string username, string password)
        {
            periodicTimer = new PeriodicTimer(timespan);
            Username = username;
            Password = password;
        }

        public void Start()
        {
            task = Do();
        }

        private async Task Do()
        {
            try
            {
                while(await periodicTimer.WaitForNextTickAsync(cancellationSource.Token))
                {
                    LoginWorker.Login(Username, Password);
                }
            }
            catch (Exception ignore)
            {
            }
        }

        public async Task Stop()
        {
            if (task != null)
            {
                cancellationSource.Cancel();
                await task;
                cancellationSource.Dispose();
            }
        }
    }
}
