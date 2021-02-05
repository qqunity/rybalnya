package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func GetListOfPostLikesByPostID(db *pg.DB, postID uint64, shouldCloseDB bool) (likes []models.Like, exitCode uint) {
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
		err = db.Model(&likes).
			Where("post_id = ?", postID).
			Select()
		if err != nil {
			if err.Error() == "pg: no rows in result set"{
				exitCode = 1
			} else {
				panic(fmt.Errorf("FAIL. Fatal error with get likes count: %s", err))
			}
		}
		if likes == nil {
			exitCode = 1
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return likes, exitCode
}
