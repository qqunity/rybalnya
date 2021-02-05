package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func DeleteLike(db *pg.DB, postID uint64, userID uint64, shouldCloseDB bool) (exitCode uint) {
	like := new(models.Like)
	if exitCode == 0 {
		err := db.Model(like).
			Where("(post_id, user_id) = (?, ?)", postID, userID).
			Select()
		if err != nil {
			if err.Error() == "pg: no rows in result set"{
				exitCode = 1
			} else {
				panic(fmt.Errorf("FAIL. Fatal error with get like: %s", err))
			}
		} else {
			_, err = db.Model(like).
				Where("(post_id, user_id) = (?, ?)", postID, userID).
				Delete()
			if err != nil {
				panic(fmt.Errorf("FAIL. Fatal error with delete like in db: %s", err))
			}
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return exitCode
}
