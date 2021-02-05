package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func DeleteUserInfoByEmail(db *pg.DB, email string, shouldCloseDB bool) (exitCode uint) {
	user := new(models.User)
	user, exitCode = GetUserInfoByEmail(db, email, false)
	if exitCode == 0 {
		_, err := db.Model(user).
			WherePK().
			Delete()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add user in db: %s", err))
		}
	}
	if shouldCloseDB{
		defer db.Close()
	}
	return exitCode
}
