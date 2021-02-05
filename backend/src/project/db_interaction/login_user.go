package db_interaction

import (
	"github.com/go-pg/pg/v10"
)

func LoginUser(db *pg.DB, email string, pass string, shouldCloseDB bool) (isLoginSuccess bool, exitCode uint) {
	exitCode = 0
	isLoginSuccess = false
	user, exitCode := GetUserInfoByEmail(db, email, false)
	if exitCode == 0 {
		if user.Password == pass {
			isLoginSuccess = true
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return isLoginSuccess, exitCode
}
