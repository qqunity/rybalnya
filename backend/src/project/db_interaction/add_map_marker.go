package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func AddMapMarker(db *pg.DB, markerInfo *models.MapInfo, shouldCloseDB bool)  {
	_, err := db.Model(markerInfo).Insert()
	if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add marker in db: %s", err))
		}
	if shouldCloseDB {
		defer db.Close()
	}
}
