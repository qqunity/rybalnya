package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func GetMarkersInfo(db *pg.DB, userId int64, shouldCloseDB bool) (markersInfo []models.MapInfo, exitCode uint) {
	exitCode = 0
	err := db.Model(&markersInfo).
		Where("user_id = ?", userId).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get map marker info: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return markersInfo, exitCode
}
