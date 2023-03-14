using AutoRelogin.Database;

namespace AutoRelogin;

public partial class MainPage : ContentPage
{
    public string _username, _password;
    public string Username
    {
        get
        {
            return _username;
        }

        set {
            _username = value;
            OnPropertyChanged(nameof(Username));
        }
    }

    public string Password
    {
        get
        {
            return _password;
        }

        set
        {
            _password = value;
            OnPropertyChanged(nameof(Password));
        }
    }

    public ConfigDatabase configDatabase { get; set; } = new();

    public BackgroundLoginTask BackgroundLoginTask { get; set; }
    public MainPage()
    {
        BindingContext = this;
        InitializeComponent();
        loadData();
    }

    public async void loadData()
    {
        ConfigEntry usernameConfig = await configDatabase.GetItemAsync("login_username");

        ConfigEntry passwordConfig = await configDatabase.GetItemAsync("login_password");

        if (usernameConfig != null)
            Username = usernameConfig.Value;

        if (passwordConfig != null)
            Password = passwordConfig.Value;
    }

    private async void Button_Clicked(object sender, EventArgs e)
    {
        // Add saving stuff.
        ConfigEntry usernameEntry = new()
        {
            Name = "login_username",
            Value = Username
        };

        ConfigEntry passwordEntry = new()
        {
            Name = "login_password",
            Value = Password
        };

        await configDatabase.SaveItemAsync(usernameEntry);
        await configDatabase.SaveItemAsync(passwordEntry);

        NetworkAccess networkAccess = Connectivity.Current.NetworkAccess;
        if (!networkAccess.HasFlag(NetworkAccess.ConstrainedInternet) && !networkAccess.HasFlag(NetworkAccess.Internet))
        {
            await DisplayAlert("Notification", "It looks like you do not have a active connection, please connect to the OSZ-Imt Network!", "OK");
            return;
        }

        if (!DeviceInfo.Current.Platform.Equals(DevicePlatform.Android) && !DeviceInfo.Current.Platform.Equals(DevicePlatform.iOS))
        {
            await DisplayAlert("Notification", "You will not receive any information about the login process!", "OK");
        }

        LoginWorker.Login(Username, Password);

        BackgroundLoginTask = new BackgroundLoginTask(TimeSpan.FromSeconds(3), Username, Password);
        BackgroundLoginTask.Start();
    }

    private async void Button_Clicked_1(object sender, EventArgs e)
    {
        await configDatabase.DeleteItemAsync(await configDatabase.GetItemAsync("login_username"));
        await configDatabase.DeleteItemAsync(await configDatabase.GetItemAsync("login_password"));
    }

    private void Entry_TextChanged_1(object sender, TextChangedEventArgs e)
    {

    }

    private void Entry_TextChanged(object sender, TextChangedEventArgs e)
    {

    }
}

