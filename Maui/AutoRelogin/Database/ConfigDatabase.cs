using SQLite;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AutoRelogin.Database
{
    public class ConfigDatabase
    {
        SQLiteAsyncConnection Database;

        async Task Init()
        {
            if (Database is not null)
                return;

            Database = new SQLiteAsyncConnection(Constants.DatabasePath, Constants.Flags);
            var result = await Database.CreateTableAsync<ConfigEntry>();
        }

        public async Task<List<ConfigEntry>> GetItemsAsync()
        {
            await Init();
            return await Database.Table<ConfigEntry>().ToListAsync();
        }

        public async Task<ConfigEntry> GetItemAsync(string name)
        {
            await Init();
            return await Database.Table<ConfigEntry>().Where(i => i.Name == name).FirstOrDefaultAsync();
        }

        public async Task<ConfigEntry> GetItemAsync(int id)
        {
            await Init();
            return await Database.Table<ConfigEntry>().Where(i => i.Id == id).FirstOrDefaultAsync();
        }

        public async Task<int> SaveItemAsync(ConfigEntry item)
        {
            await Init();
            ConfigEntry entry = await GetItemAsync(item.Name);
            
            if (entry != null)
            {
                item.Id = entry.Id;
                return await Database.UpdateAsync(item);
            }
            else
                return await Database.InsertAsync(item);
        }

        public async Task<int> DeleteItemAsync(ConfigEntry item)
        {
            await Init();
            return await Database.DeleteAsync(item);
        }
    }
}
