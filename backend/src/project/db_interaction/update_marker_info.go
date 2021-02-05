package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func UpdateMapInfo(db *pg.DB, id string, newMarkerInfo *models.MapInfo, shouldCloseDB bool) (someMarkerInfo *models.MapInfo, exitCode uint) {
	updatedMarkerInfo := new(models.MapInfo)
	exitCode = 0
	var markerInfo models.MapInfo
	err := db.Model(&markerInfo).
		Where("id = ?", id).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get map marker info: %s", err))
		}
	}
	if exitCode == 0 {
		_, err := db.Model(someMarkerInfo).
			Set("info = ?", func() string { if newMarkerInfo.Info != "" { return newMarkerInfo.Info } else { return markerInfo.Info } }()).
			Set("image = ?", func() []byte { if newMarkerInfo.Image != nil { return newMarkerInfo.Image } else { return markerInfo.Image } }()).
			Set("lat = ?", func() float64 { if newMarkerInfo.Lat != 0 { return newMarkerInfo.Lat } else { return markerInfo.Lat } }()).
			Set("long = ?", func() float64 { if newMarkerInfo.Long != 0 { return newMarkerInfo.Long } else { return markerInfo.Long } }()).
			Set("user_id = ?", markerInfo.UserID).
			Where("id = ?", markerInfo.ID).
			Update()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with update user info in db: %s", err))
		}
		err = db.Model(updatedMarkerInfo).Where("id = ?", markerInfo.ID).Select()
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return updatedMarkerInfo, exitCode
}
