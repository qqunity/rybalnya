package db_interaction

import (
	"fmt"

	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func GetAllUsersInfo(db *pg.DB, shouldCloseDB bool) (users []*models.User, exitCode uint) {
	exitCode = 0
	err := db.Model(&users).Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get all users info: %s", err))
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
func GetAllUsersInfoWithOnline(db *pg.DB, shouldCloseDB bool) (onlineInfo []bool, users []*models.User, exitCode uint) {
	exitCode = 0
	var online []*models.Online
	err := db.Model(&online).Order("is_online DESC").Column("online.*").Relation("User").Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get all users info: %s", err))
		}
	}
	for _, element := range online {
		users = append(users, element.User)
		onlineInfo = append(onlineInfo, element.IsOnline)
	}
	if online == nil {
		exitCode = 1
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return onlineInfo, users, exitCode
}
