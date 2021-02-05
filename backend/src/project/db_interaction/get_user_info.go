package db_interaction

import (
	"fmt"

	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func GetUserInfoByEmail(db *pg.DB, email string, shouldCloseDB bool) (user *models.User, exitCode uint) {
	user = new(models.User)
	exitCode = 0
	err := db.Model(user).
		Where("email = ?", email).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get user info: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return user, exitCode
}

func GetUsersInfoByNickname(db *pg.DB, nickname string, shouldCloseDB bool) (users []models.User, exitCode uint) {
	exitCode = 0
	err := db.Model(&users).
		Where("nickname = ?", nickname).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get users info: %s", err))
		}
	}
	if users == nil {
		exitCode = 1
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return users, exitCode
}

func GetUserInfoByUserId(db *pg.DB, userID uint64, shouldCloseDB bool) (user *models.User, exitCode uint) {
	user = new(models.User)
	exitCode = 0
	err := db.Model(user).
		Where("id = ?", userID).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get user info: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return user, exitCode
}

// func GetUserInfoByEmailWithOnline(db *pg.DB, email string, shouldCloseDB bool) (user *models.User, exitCode uint) {
// 	user = new(models.User)
// 	exitCode = 0
// 	err := db.Model(user).
// 		Where("email = ?", email).Column("user.*").Relation("Online").
// 		Select()
// 	if err != nil {
// 		if err.Error() == "pg: no rows in result set" {
// 			exitCode = 1
// 		} else {
// 			panic(fmt.Errorf("FAIL. Fatal error with get user info: %s", err))
// 		}
// 	}
// 	if shouldCloseDB {
// 		defer db.Close()
// 	}
// 	return user, exitCode
// }
