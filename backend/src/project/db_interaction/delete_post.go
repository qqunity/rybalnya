package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func DeletePost(db *pg.DB, id string, shouldCloseDB bool) (exitCode uint) {
	post := new(models.Post)
	if exitCode == 0 {
		err := db.Model(post).
			Where("id = ?", id).
			Select()
		if err != nil {
			if err.Error() == "pg: no rows in result set"{
				exitCode = 1
			} else {
				panic(fmt.Errorf("FAIL. Fatal error with post users info: %s", err))
			}
		} else {
			_, err = db.Model(post).
				Where("id = ?", id).
				Delete()
			if err != nil {
				panic(fmt.Errorf("FAIL. Fatal error with delete post in db: %s", err))
			}
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return exitCode
}
