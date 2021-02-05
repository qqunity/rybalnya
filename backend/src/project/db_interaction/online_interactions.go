package db_interaction

import (
	"fmt"

	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func UpdateUserOnline(db *pg.DB, userId uint64, isOnline bool, shouldCloseDB bool) (exitCode uint) {
	exitCode = 0
	oldOnline := new(models.Online)
	err := db.Model(oldOnline).
		Where("user_id = ?", userId).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			newOnline := new(models.Online)
			newOnline.IsOnline = isOnline
			newOnline.UserID = userId
			_, err := db.Model(newOnline).
				Insert()
			if err != nil {
				panic(fmt.Errorf("FAIL. Fatal error with add User in Online table in db: %s", err))
			}
			return exitCode
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with update comment info: %s", err))
		}
	}

	online := new(models.Online)
	if exitCode == 0 {
		_, err := db.Model(online).
			Set("user_id = ?", oldOnline.UserID).
			Set("is_online = ?", isOnline).
			Where("user_id = ?", userId).
			Update()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with update msg seen info in db: %s", err))
		}
	}

	if shouldCloseDB {
		defer db.Close()
	}
	return exitCode
}

func IsUserOnline(db *pg.DB, userId uint64, shouldCloseDB bool) (isOnline bool, exitCode uint) {
	online := new(models.Online)
	err := db.Model(online).
		Where("user_id = ?", userId).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with update comment info: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return online.IsOnline, exitCode
}
