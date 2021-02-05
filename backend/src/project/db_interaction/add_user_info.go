package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func AddUserInfo(db *pg.DB, user *models.User, shouldCloseDB bool) (exitCode uint) {
	_, exitCode = GetUserInfoByEmail(db, user.Email, false)
	if exitCode != 0 {
		_, err := db.Model(user).
			Insert()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add user in db: %s", err))
		}
	}
	if shouldCloseDB{
		defer db.Close()
	}
	return exitCode
}
