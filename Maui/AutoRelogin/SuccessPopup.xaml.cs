using Mopups.Pages;

namespace AutoRelogin;

public partial class SuccessPopup : PopupPage
{
	public string Message { get; set; }
	public SuccessPopup()
	{
		BindingContext = this;
		InitializeComponent();
	}
}