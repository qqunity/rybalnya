package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func UpdateUserInfo(db *pg.DB, newUserInfo *models.User, shouldCloseDB bool) (exitCode uint) {
	var user *models.User
	user, exitCode = GetUserInfoByEmail(db, newUserInfo.Email, false)
	if exitCode == 0 {
		_, err := db.Model(user).
			Set("full_name = ?", func() string { if newUserInfo.FullName != "" { return newUserInfo.FullName } else { return user.FullName } }()).
			Set("nickname = ?", func() string { if newUserInfo.Nickname != "" { return newUserInfo.Nickname } else { return user.Nickname } }()).
			Set("email = ?", user.Email).
			Set("password = ?", func() string { if newUserInfo.Password != "" { return newUserInfo.Password } else { return user.Password } }()).
			Set("about = ?", func() string { if newUserInfo.About != "" { return newUserInfo.About } else { return user.About } }()).
			Set("image = ?", func() []byte { if newUserInfo.Image != nil { return newUserInfo.Image } else { return user.Image } }()).
			Where("id = ?", user.ID).
			Update()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with update user info in db: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return exitCode
}
