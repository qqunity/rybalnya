package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func DeleteMarkerInfo(db *pg.DB, id string, shouldCloseDB bool) (exitCode uint) {
	markerInfo := new(models.MapInfo)
	if exitCode == 0 {
		err := db.Model(markerInfo).
			Where("id = ?", id).
			Select()
		if err != nil {
			if err.Error() == "pg: no rows in result set"{
				exitCode = 1
			} else {
				panic(fmt.Errorf("FAIL. Fatal error with get users info: %s", err))
			}
		} else {
			_, err = db.Model(markerInfo).
				Where("id = ?", id).
				Delete()
			if err != nil {
				panic(fmt.Errorf("FAIL. Fatal error with delete markerInfo in db: %s", err))
			}
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return exitCode
}
