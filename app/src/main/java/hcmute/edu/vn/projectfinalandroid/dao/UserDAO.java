package hcmute.edu.vn.projectfinalandroid.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import hcmute.edu.vn.projectfinalandroid.model.User;

@Dao
public interface UserDAO {
    @Insert
    void insert(User user);
    @Query("SELECT COUNT(*) FROM user WHERE email = :email OR name_account = :accountName")
    int isEmailOrAccountExist(String email, String accountName);

    @Query("SELECT * FROM user WHERE name_account = :nameAcount LIMIT 1")
    User getUserByNameAccount(String nameAcount);


}
