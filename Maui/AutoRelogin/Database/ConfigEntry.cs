using SQLite;

namespace AutoRelogin.Database
{
    [Table("Config")]
    public class ConfigEntry
    {
        [PrimaryKey]
        [AutoIncrement]
        public int Id { get; set; }
        public string Name { get; set; }
        public string Value { get; set; }
    }
}
