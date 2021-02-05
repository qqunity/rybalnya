package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func GetCommentsByPostID(db *pg.DB, postID uint64, shouldCloseDB bool) (comments []models.Comment, exitCode uint) {
	exitCode = 0
	post := new(models.Post)
	err := db.Model(post).
		Where("id = ?", postID).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set"{
			exitCode = 2
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get likes count: %s", err))
		}
	}
	if exitCode == 0 {
		err = db.Model(&comments).
			Where("post_id = ?", postID).
			Order("created_at DESC").
			Select()
		if err != nil {
			if err.Error() == "pg: no rows in result set"{
				exitCode = 1
			} else {
				panic(fmt.Errorf("FAIL. Fatal error with get likes count: %s", err))
			}
		}
		if comments == nil {
			exitCode = 1
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return comments, exitCode
}
